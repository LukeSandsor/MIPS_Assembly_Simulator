import java.io.*;
import java.util.*;

//Lucas Reyna & Lucas Sandsor
public class lab4 
{
    private static Map<Integer, String[]> instructionList = new HashMap<>();
    private static Map<String, Integer> labels = new HashMap<>();

    public static void main(String[] args) throws FileNotFoundException {
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
            int PC = Lines.getLineNumber();

            String instruction = Lines.getLine();
            instruction = instruction.replaceAll(",", " ");
            instruction = instruction.replaceAll("\\$", " \\$");
            instruction = instruction.replaceAll("\t", "");
            instruction = instruction.replaceAll("\\(", " ");
            instruction = instruction.replaceAll("\\)", "");
            String[] instructionArr = instruction.split("\\s+");

            instructionList.put(PC, instructionArr);
        }

        Emulate emulator = new Emulate(instructionList, labels);
        MyPipe pipe = new MyPipe();

        while (true)
        {
            System.out.print("mips> ");

            String inputLine = scanIn.nextLine();
            String[] inputs = inputLine.split(" ");

            if (args.length == 2)
                System.out.println(inputLine);

            if (inputs.length == 3)
                mipsInput(emulator, pipe, inputs[0], Integer.parseInt(inputs[1]), Integer.parseInt(inputs[2]));

            else if (inputs.length == 2)
                mipsInput(emulator, pipe, inputs[0], Integer.parseInt(inputs[1]), -1);
            else
                mipsInput(emulator, pipe, inputs[0], 1, -1);
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

    private static void mipsInput(Emulate e, MyPipe p, String input, int num1, int num2)
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
                e.clearMipsValues();
                System.out.println("        Simulator reset");
                break;
            case "d":
                System.out.printf("pc = %d\n", e.PC);
                System.out.printf("$0 = %d          $v0 = %d            $v1 = %d            $a0 = %d\n",
                        e.registerFile[0],e.registerFile[2],e.registerFile[3],e.registerFile[4]);
                System.out.printf("$a1 = %d          $a2 = %d            $a3 = %d            $t0 = %d\n",
                        e.registerFile[5],e.registerFile[6],e.registerFile[7],e.registerFile[8]);
                System.out.printf("$t1 = %d          $t2 = %d            $t3 = %d            $t4 = %d\n",
                        e.registerFile[9],e.registerFile[10],e.registerFile[11],e.registerFile[12]);
                System.out.printf("$t5 = %d          $t6 = %d            $t7 = %d            $s0 = %d\n",
                        e.registerFile[13],e.registerFile[14],e.registerFile[15],e.registerFile[16]);
                System.out.printf("$s1 = %d          $s2 = %d            $s3 = %d            $s4 = %d\n",
                        e.registerFile[17],e.registerFile[18],e.registerFile[19],e.registerFile[20]);
                System.out.printf("$s5 = %d          $s6 = %d            $s7 = %d            $t8 = %d\n",
                        e.registerFile[21],e.registerFile[22],e.registerFile[23],e.registerFile[24]);
                System.out.printf("$t9 = %d          $sp = %d            $ra = %d\n",
                        e.registerFile[25],e.registerFile[29],e.registerFile[31]);
                break;
            case "m":
                for(int i = num1; i <= num2; i++) {
                    System.out.printf("[%d] = %d\n", i, e.dataMem[i]);
                }
                break;
            case "s":
                int i;
                for (i = 0; i < num1; i++)
                {
                    processCycle(e, p, input);
                }
                break;
            case "r":
                while (p.PC < instructionList.size())
                {
                    processCycle(e, p, input);
                }
                p.cycles += 4;

                System.out.println("\n" + "Program complete");
                System.out.printf("CPI = %2.3f Cycles = %d Instructions = %d \n\n"
                        , p.getCPI(), p.cycles, p.instructions);
                break;
            case "p":
                System.out.println("\n" + "pc      if/id   id/exe  exe/mem mem/wb");
                System.out.println(p.PC+"       "+ p.ifid[0]+"    "+
                        p.idexe[0]+"    "
                        +p.exemem[0]+"    "+p.memwb[0] + "\n");
        }
    }

    private static void processCycle(Emulate e, MyPipe p, String input)
    {
        int prevPPC;
        int prevEPC;

        prevEPC = e.PC;

        if (e.PC < instructionList.size())
        {
            e.sInstruction();
            if (prevEPC + 1 != e.PC) //jump or branch occurred
            {
                p.jumpQueue.offerLast(e.PC);
                
                /*int i = 0;
                System.out.println(p.jumpQueue.size());
                while (i < p.jumpQueue.size())
                {
                    System.out.println("JumpQueue " + i + " : " + p.jumpQueue.get(i));
                    i++;
                }*/
            }  
        }
        if (p.PC < instructionList.size())
            p.stepNext(instructionList.get(p.PC));

        /*System.out.println("prevE PC: " + prevEPC);
        System.out.println("prevP PC: " + prevEPC);
        System.out.println("jump PC: " + p.jumpPC);

        System.out.println("E PC: " + e.PC);
        System.out.println("P PC: " + p.PC); */
        
        if (p.branchFlag > 0)
            p.branchFlag--;
        else
            p.branchFlag = e.branchFlag;

        if (p.ifid[0].equals("squash"))
        {
            p.setPC(p.jumpQueue.poll());
        }
        if (input.equals("s"))
        {
            System.out.println("\n" + "pc      if/id   id/exe  exe/mem mem/wb");
            System.out.println(p.PC+"       "+ p.ifid[0]+"    "+
                    p.idexe[0]+"    "
                    +p.exemem[0]+"    "+p.memwb[0] + "\n");
        }
    }
}
