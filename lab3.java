import java.io.*;
import java.util.*;

//Lucas Reyna & Lucas Sandsor
public class lab3 
{
    private static Map<String, String> instructionCodes = new HashMap<>();
    private static Map<Integer, String[]> instructionList = new HashMap<>();
    private static Map<String, Integer> labels = new HashMap<>();
    private static int PC = 0;
    private static int dataMem[] = new int[8192];
    private static int registerFile[] = new int[32];

    public static void main(String[] args) throws FileNotFoundException {
        makeMap();
        Set<String> keySet = instructionCodes.keySet();

        File file1 = new File(args[0]);
        Scanner scan1 = new Scanner(file1);
        Scanner scanIn;

        if (args.length == 2)
        {
            File file2 = new File(args[1]);
            scanIn = new Scanner(file2);
        }
        else
            scanIn = new Scanner(System.in);
        List<line> lineList = new ArrayList<>();

        Integer i = 0;
        while (scan1.hasNextLine()) {
            String newLine = scan1.nextLine();
            if (newLine.equals("#") || newLine.equals(":")) {
            } else {
                List<String> splitByComment = Arrays.asList(newLine.split("#"));
                newLine = splitByComment.get(0);
                List<String> splitByLabel = Arrays.asList(newLine.split(":"));
                if (!isblank(newLine) && !isblank(splitByComment.get(0))) {
                    if (newLine.contains(":") && splitByLabel.size() == 1) {
                        //System.out.println(splitByLabel.get(0) + "           " + i);
                        labels.put(splitByLabel.get(0), i);
                        i--;
                    } else if (splitByLabel.size() > 1) {
                        if (isblank(splitByLabel.get(1))) {
                            labels.put(splitByLabel.get(0), i);
                            i--;
                        } else {
                            newLine = splitByLabel.get(1);
                            //System.out.println(splitByLabel.get(0) + "           " + i);
                            labels.put(splitByLabel.get(0), i);
                            lineList.add(new line(i, newLine));
                        }
                    } else {
                        lineList.add(new line(i, newLine));
                    }
                    i++;
                }
            }
        }
        scan1.close();

        for (line Lines : lineList) {
            PC = Lines.getLineNumber();

            String instruction = Lines.getLine();
            instruction = instruction.replaceAll(",", " ");
            instruction = instruction.replaceAll("\\$", " \\$");
            instruction = instruction.replaceAll("\t", "");
            instruction = instruction.replaceAll("\\(", " ");
            instruction = instruction.replaceAll("\\)", "");
            String[] instructionArr = instruction.split("\\s+");

            if (!keySet.contains(instructionArr[0])) {
                System.out.println("invalid instruction: " + instructionArr[0]);
                return;
            }

            instructionList.put(PC, instructionArr);
        }

        PC = 0;
        while (true)
        {
            System.out.print("mips> ");

            String inputLine = scanIn.nextLine();
            String[] inputs = inputLine.split(" ");

            if (args.length == 2)
                System.out.println(inputLine);

            if (inputs.length == 3)
                mipsInput(inputs[0], Integer.parseInt(inputs[1]), Integer.parseInt(inputs[2]));

            else if (inputs.length == 2)
                mipsInput(inputs[0], Integer.parseInt(inputs[1]), -1);
            else
                mipsInput(inputs[0], -1, -1);
        }
    }

    private static void mipsInput(String input, int num1, int num2)
    {
        switch(input)
        {
            case "q":
                System.exit(0);
                break;
            case "h":
                System.out.println("h = show help");
                System.out.println("d = dump register state");
                System.out.println("s = single step through the program (i.e. execute 1 instruction and stop)");
                System.out.println("s num = step through num instructions of the program");
                System.out.println("r = run until the program ends");
                System.out.println("m num1 num2 = display data memory from location num1 to num2");
                System.out.println("c = clear all registers, memory, and the program counter to 0");
                System.out.println("q = exit the program");
                break;
            case "c":
                clearMipsValues();
                System.out.println("        Simulator reset");
                break;
            case "d":
                System.out.printf("pc = %d\n", PC);
                System.out.printf("$0 = %d          $v0 = %d            $v1 = %d            $a0 = %d\n",
                        registerFile[0],registerFile[2],registerFile[3],registerFile[4]);
                System.out.printf("$a1 = %d          $a2 = %d            $a3 = %d            $t0 = %d\n",
                        registerFile[5],registerFile[6],registerFile[7],registerFile[8]);
                System.out.printf("$t1 = %d          $t2 = %d            $t3 = %d            $t4 = %d\n",
                        registerFile[9],registerFile[10],registerFile[11],registerFile[12]);
                System.out.printf("$t5 = %d          $t6 = %d            $t7 = %d            $s0 = %d\n",
                        registerFile[13],registerFile[14],registerFile[15],registerFile[16]);
                System.out.printf("$s1 = %d          $s2 = %d            $s3 = %d            $s4 = %d\n",
                        registerFile[17],registerFile[18],registerFile[19],registerFile[20]);
                System.out.printf("$s5 = %d          $s6 = %d            $s7 = %d            $t8 = %d\n",
                        registerFile[21],registerFile[22],registerFile[23],registerFile[24]);
                System.out.printf("$t9 = %d          $sp = %d            $ra = %d\n",
                        registerFile[25],registerFile[29],registerFile[31]);
                break;
            case "m":
                for(int i = num1; i <= num2; i++) {
                    System.out.printf("[%d] = %d\n", i, dataMem[i]);
                }
                break;
            case "s":
                if (num1 > 1) {
                    sInstruction(num1);
                    System.out.printf("%d instruction(s) executed\n", num1);
                }
                else {
                    sInstruction();
                    System.out.printf("%d instruction(s) executed\n", 1);
                }
                break;
            case "r":
                rInstruction();
                break;
        }
    }

    private static void clearMipsValues()
    {
        Arrays.fill(registerFile, 0);
        Arrays.fill(dataMem, 0);
        PC = 0;
    }

    private static void sInstruction(int numSteps)
    {
        int i = 0;
        while (i < numSteps) {
            String[] instruction = instructionList.get(PC);
            String mValue = instructionCodes.get(instruction[0]);
            String[] mValues = mValue.split(",");

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
            i++;
        }
    }

    private static void sInstruction()
    {
        String[] instruction = instructionList.get(PC);
        String mValue = instructionCodes.get(instruction[0]);
        String[] mValues = mValue.split(",");

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
    }


    private static void rInstruction()
    {
        while (PC < instructionList.size()) {
            String[] instruction = instructionList.get(PC);
            String mValue = instructionCodes.get(instruction[0]);
            String[] mValues = mValue.split(",");

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
        }
    }

    private static boolean isblank(String str)
    {
        if(str == null || str.trim().length() == 0)
        {
            return true;
        }
        return false;
    }

    private static void makeMap()
    {
        // Each instruction has an OpCode / Funct Binary num represented in String format
        instructionCodes.put("and", "r,100100");
        instructionCodes.put("or", "r,100101");
        instructionCodes.put("add", "r,100000");
        instructionCodes.put("addi", "i,001000"); // { 16{immediate[15]}, immediate }
        instructionCodes.put("sll", "r,000000");
        instructionCodes.put("sub", "r,100010");
        instructionCodes.put("slt", "r,101010");
        instructionCodes.put("beq", "i,000100"); // { 14{immediate[15]}, immediate, 2’b0 }
        instructionCodes.put("bne", "i,000101"); // { 14{immediate[15]}, immediate, 2’b0 }
        instructionCodes.put("lw", "i,100011");
        instructionCodes.put("sw", "i,101011");
        instructionCodes.put("j", "j,000010"); // J-Type is opcode + address
        instructionCodes.put("jr", "j,000000");
        instructionCodes.put("jal", "j,000011"); //{ PC+4[31:28], address, 2’b0 }
    }

    private static void jumpOperation(String[] instruction, String[] mValues)
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

    private static void calculationOperation(String[] instruction, String[] mValues)
    {
        int regFileIndex = registerIndex(instruction[1]);
        registerFile[regFileIndex] = ALU(instruction, mValues, 2, 3);
    }

    private static void memoryOperation(String[] instruction, String[] mValues)
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

    private static void processBranch(String[] instruction)
    {
        int labelDiff;

        labelDiff = labels.get(instruction[3]) - PC - 1;
        // minus 1 for the immediate Calc
        PC = PC + labelDiff;
    }

    private static void branchOperation(String[] instruction, String[] mValues)
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

    private static int ALU(String[] instruction, String[] mValues, int regAPosition, int regBPosition)
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

    private static int registerIndex(String argument)
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
}
