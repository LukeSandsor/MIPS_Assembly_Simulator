import java.io.*;
import java.util.*;

//Lucas Reyna & Lucas Sandsor
public class Emulate 
{
    public Map<Integer, String[]> instructionList;
    public Map<String, Integer> labels;
    public int PC;
    public int totalInstructions;
    public int totalCycles;
    public int dataMem[];
    public int registerFile[];
    private Map<String, String> instructionCodes;
    public int branchFlag;

    public Emulate(Map<Integer, String[]> instructionList, Map<String, Integer> labels)
    {
        this.instructionList = instructionList;
        this.labels = labels;
        this.PC = 0;
        this.dataMem = new int[8192];
        this.registerFile = new int[32];
        this.instructionCodes = makeMap();
        this.branchFlag = 0;
        this.totalInstructions = 0;
    }

    public void sInstruction()
    {
        String[] instruction = instructionList.get(PC);
        String mValue = instructionCodes.get(instruction[0]);
        String[] mValues = mValue.split(",");

        branchFlag = 0;

        switch (mValues[0]) {
            case "r":
                calculationOperation(instruction, mValues);
                break;
            case "i":
                if (instruction[0].charAt(0) == 'b')
                    branchOperation(instruction, mValues);
                else if (instruction[0].charAt(1) == 'w') {
                    memoryOperation(instruction, mValues);
                } else
                    calculationOperation(instruction, mValues);
                break;
            case "j":
                jumpOperation(instruction, mValues);
                break;
        }
        PC++;
        totalInstructions++;
    }

    private Map<String, String> makeMap()
    {
        Map<String, String> iCodes = new HashMap<>();

        // Each instruction has an OpCode / Funct Binary num represented in String format
        iCodes.put("and", "r,100100");
        iCodes.put("or", "r,100101");
        iCodes.put("add", "r,100000");
        iCodes.put("addi", "i,001000"); // { 16{immediate[15]}, immediate }
        iCodes.put("sll", "r,000000");
        iCodes.put("sub", "r,100010");
        iCodes.put("slt", "r,101010");
        iCodes.put("beq", "i,000100"); // { 14{immediate[15]}, immediate, 2’b0 }
        iCodes.put("bne", "i,000101"); // { 14{immediate[15]}, immediate, 2’b0 }
        iCodes.put("lw", "i,100011");
        iCodes.put("sw", "i,101011");
        iCodes.put("j", "j,000010"); // J-Type is opcode + address
        iCodes.put("jr", "j,000000");
        iCodes.put("jal", "j,000011"); //{ PC+4[31:28], address, 2’b0 }

        return iCodes;
    }

    private void jumpOperation(String[] instruction, String[] mValues)
    {
        switch (mValues[1])
        {
            case "000000": // jr
            {
                PC = registerFile[registerIndex(instruction[1])] - 1;
                break;
            }
            case "000010": // j
            {
                PC = (labels.get(instruction[1]) - 1); // -1 because of PC++
                break;
            }
            case "000011": // jal
            {
                registerFile[31] = PC + 1;
                PC = (labels.get(instruction[1]) - 1);
                break;
            }
        }
    }

    private void calculationOperation(String[] instruction, String[] mValues)
    {
        int regFileIndex = registerIndex(instruction[1]);
        registerFile[regFileIndex] = ALU(instruction, mValues, 2, 3);
    }

    private void memoryOperation(String[] instruction, String[] mValues)
    {
        int RI = registerIndex(instruction[1]);
        int MemAddress = ALU(instruction, mValues, 3, 2);

        if (instruction[0].equals("lw"))
        {
            registerFile[RI] = dataMem[MemAddress];
        }
        else if (instruction[0].equals("sw"))
        {
            dataMem[MemAddress] = registerFile[RI];
        }
    }

    private void processBranch(String[] instruction)
    {
        int labelDiff;

        labelDiff = labels.get(instruction[3]) - PC - 1;

        // minus 1 for the immediate Calc
        PC = PC + labelDiff;
        branchFlag = 4;
    }

    private void branchOperation(String[] instruction, String[] mValues)
    {
        int regAIndex = registerIndex(instruction[1]);
        int regBIndex = registerIndex(instruction[2]);

        int regAValue = registerFile[regAIndex];
        int regBValue = registerFile[regBIndex];

        if (instruction[0].equals("beq") && regAValue == regBValue)
            processBranch(instruction);

        else if (instruction[0].equals("bne") && regAValue != regBValue)
            processBranch(instruction);
        else
            return ;
    }

    private int ALU(String[] instruction, String[] mValues, int regAPosition, int regBPosition)
    {
        String regA = instruction[regAPosition];
        String regB = instruction[regBPosition];

        int regAIndex = registerIndex(regA);

        int regAValue = registerFile[regAIndex];
        int regBValue;
        if (!(mValues[0].equals("i")))
        {
            int regBIndex = registerIndex(regB);
            regBValue = registerFile[regBIndex];
        }
        else
        {
            regBValue = Integer.parseInt(regB);
        }
        switch (mValues[1])
        {
            case "100100": // and
                return regAValue & regBValue;
            case "100101": // or
                return regAValue | regBValue;
            case "100000": // add
                return regAValue + regBValue;
            case "000000": // sll
                return regAValue << regBValue;
            case "100010": // sub
                return regAValue - regBValue;
            case "101010": // slt
                return (regAValue < regBValue) ? 1 : 0;
            default: // add for rest of instructions
                return regAValue + regBValue;
        }
    }

    private int registerIndex(String argument)
    {
        // remember to remove ',' from argument
        if (argument.charAt(0) == '$')
        {
            int registerValue = 0;
            String register = argument.substring(1);
            if (register.equals("zero") || register.equals("0"))
                registerValue = 0;
            else if (register.charAt(0) == 'v')
                registerValue = 2 + Integer.parseInt(register.substring(1));
            else if (register.charAt(0) == 'a')
                registerValue = 4 + Integer.parseInt(register.substring(1));
            else if (register.charAt(0) == 't')
            {
                int regNum = Integer.parseInt(register.substring(1));
                if (regNum < 8)
                    registerValue = 8 + regNum;
                else
                    registerValue = 24 + regNum - 8;
            }
            else if (register.equals("sp"))
                registerValue = 29;
            else if (register.charAt(0) == 's')
                registerValue = 16 + Integer.parseInt(register.substring(1));
            else
                registerValue = 31; // $ra

            return registerValue;
        }

        return -1;
    }

    public void clearMipsValues()
    {
        Arrays.fill(this.registerFile, 0);
        Arrays.fill(this.dataMem, 0);
        this.PC = 0;
    }
}
