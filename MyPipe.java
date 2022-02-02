import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

class MyPipe {
    public String[] ifid;
    public String[] idexe;
    public String[] exemem;
    public String[] memwb;
    public int PC, cycles, instructions;
    public int branchFlag;
    public LinkedList<Integer> jumpQueue = new LinkedList<Integer>();

    public MyPipe()
    {
        this.ifid = new String[] {"empty"};
        this.idexe = new String[] {"empty"};
        this.exemem = new String[] {"empty"};
        this.memwb = new String[] {"empty"};
        this.cycles = 0;
        this.PC = 0;
        this.instructions = 0;
        this.branchFlag = 0;
    }

    public double getCPI()
    {
        return ((double)cycles / instructions);
    }
    public void stepNext(String[] instructionArray)
    {
        if(ifid[0].equals("jr") || ifid[0].equals("j")
                || ifid[0].equals("jal"))
        {
            memwb = exemem;
            exemem = idexe;
            idexe = ifid;
            ifid = new String[] {"squash"};
        }
        else if((exemem[0].equals("beq") || exemem[0].equals("bne")) && branchFlag == 1)
        {
            memwb = exemem;
            idexe = new String[] {"squash"};
            exemem = new String[] {"squash"};
            ifid = new String[] {"squash"};
            
            instructions -= 3;

            branchFlag = 0;
        }
        else if(idexe[0].equals("lw") &&
                (idexe[1].equals(ifid[2])
                        || idexe[1].equals(ifid
                            [ifid.length - 1])))
        {
            memwb = exemem;
            exemem = idexe;
            idexe = new String[] {"stall"};
            PC--;
        }
        else if(instructionArray[0].equals("empty") || instructionArray[0].equals("squash") || instructionArray[0].equals("stall"))
        {
            memwb = exemem;
            exemem = idexe;
            idexe = ifid;
            ifid = instructionArray;
        }
        else
        {
            memwb = exemem;
            exemem = idexe;
            idexe = ifid;
            ifid = instructionArray;
            instructions++;
        }
        PC++;
        cycles++;
    }

    public void setPC(int newPC)
    {
        PC = newPC;
    }
}
