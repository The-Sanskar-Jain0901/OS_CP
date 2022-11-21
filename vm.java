import java.io.*;
import java.util.*;

class PCB {
    int jobId;
    int TTL;
    int TLL;
    int TTC;
    int TLC;

    PCB(String process_init) {
        this.jobId = Integer.parseInt(process_init.substring(4, 8));
        this.TTL = Integer.parseInt(process_init.substring(8, 12));
        this.TLL = Integer.parseInt(process_init.substring(12));
        this.TLC = 0;
        this.TTC = 0;
    }
}

class CPU {
    char r[];
    char ir[];
    int ic;
    boolean toggle;

    CPU() {
        this.r = new char[4];
        this.ir = new char[4];
        this.ic = 0;
        this.toggle = false;
    }
}

class vm {
    static char[][] memory;
    static char[][] virtualMemory;
    static String prv_line = "";
    static CPU cpu;
    static PCB pcb;
    static int PTR;
    static HashSet<Integer> uniqueValues;

    public static void main(String[] args) throws OSError {
        int line_exec = 0;
        int tempPtr = 0;
        File file = new File(
                "D:\\OS\\CP\\Phase1\\input.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String buffer;
            int line_no = 0;
            while ((buffer = br.readLine()) != null) {

                if (buffer.substring(0, 4).equals("$AMJ")) {
                    load();
                    tempPtr = PTR;
                    pcb = new PCB(buffer);
                } else if (buffer.substring(0, 4).equals("$DTA")) {
                    start_exec(line_no + 1);
                    break;
                } else {
                    int randNo = getRandom(0, 30);
                    while (!uniqueValues.isEmpty() && uniqueValues.contains(randNo) && randNo != PTR / 10) {
                        randNo = getRandom(0, 30);
                    }
                    uniqueValues.add(randNo);
                    memory[tempPtr][2] = (char) ((randNo / 10) + '0');
                    memory[tempPtr][3] = (char) ((randNo % 10) + '0');
                    tempPtr++;

                    if (line_exec > pcb.TLL) {
                        load();
                        PrintWriter writer = new PrintWriter("output.txt");
                        writer.print("");
                        writer.close();
                        throw new OSError("Line Limit Exceeded!! Try Again :(");
                    }
                    line_exec++;
                    instnSet(buffer);
                }

                line_no++;
            }
            for (int i = 0; i < memory.length; i++) {

                System.out.println(memory[i]);
            }
            // System.out.println(PTR);

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    // performing instructions on data cards
    private static void start_exec(int line_no) throws FileNotFoundException, IOException, OSError {

        try (BufferedReader br = new BufferedReader(new FileReader("D:\\OS\\CP\\Phase1\\input.txt"))) {
            // skipping n lines
            for (int i = 0; i < line_no; i++)
                br.readLine();
            for (int i = 0; i < 100; i++) {
                String line = "";
                String inst = virtualMemory[i][0] + "" + virtualMemory[i][1];

                if (virtualMemory[i].toString().equals("    "))
                    break;
                if (pcb.TTC > pcb.TTL) {
                    load();
                    PrintWriter writer = new PrintWriter("output.txt");
                    writer.print("");
                    writer.close();
                    throw new OSError("Time Limit Exceeded!! Try Again :(");
                }

                if (inst.equals("GD")) {
                    if ((line = br.readLine()) != null && !(line).substring(0, 4).equals("$END")) {

                        mos(virtualMemory[i], line);
                    } else if ((line.substring(0, 4).equals("$END"))) {

                        System.out.println("OUT OF DATA ERROR");
                        load();
                        PrintWriter writer = new PrintWriter("output.txt");
                        writer.print("");
                        writer.close();
                        return;
                    }

                    pcb.TTC += 2;
                } else if (inst.equals("PD")) {
                    // System.out.println(inst);
                    mos(virtualMemory[i], line);
                    pcb.TTC++;
                } else if (inst.charAt(0) == 'H') {
                    mos(virtualMemory[i], line);

                    pcb.TTC++;
                } else if (inst.equals("LR") || inst.equals("SR") || inst.equals("CR"))

                {
                    user_program(virtualMemory[i]);
                    if (inst.equals("SR"))
                        pcb.TTC += 2;
                    else
                        pcb.TTC++;
                } else if (inst.equals("BT")) {
                    user_program(virtualMemory[i]);
                    if (cpu.toggle) {
                        i = (cpu.ir[2] - '0') * 10 + (cpu.ir[3] - '0');
                        i--;
                    }

                } else if (!inst.equals("**")) {
                    // System.out.println(inst);
                    System.out.println("OPCODE ERROR");
                    load();
                    PrintWriter writer = new PrintWriter("output.txt");
                    writer.print("");
                    writer.close();
                    break;
                } else {
                    break;
                }

            }
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private static void user_program(char[] instruction) throws IOException, OSError {
        // lr,sr,cr,bt
        cpu.ir = instruction;
        String inst = cpu.ir[0] + "" + cpu.ir[1];
        if ((cpu.ir[2] > 58 || cpu.ir[2] < 48) || (cpu.ir[3] > 58 || cpu.ir[3] < 48)) {
            System.out.println("OPERAND ERROR");
            load();
            PrintWriter writer = new PrintWriter("output.txt");
            writer.print("");
            writer.close();
            return;
        }
        int add = (cpu.ir[2] - '0') * 10 + (cpu.ir[3] - '0');
        if (inst.equals("BT")) {
            // System.out.println(virtualMemory[add - 1]);
            if (cpu.toggle) {
                if (virtualMemory[add][0] == '*' && virtualMemory[add][1] == '*') {
                    System.out.println("NO INSTRUCTION EXISTS");
                    load();
                    PrintWriter writer = new PrintWriter("output.txt");
                    writer.print("");
                    writer.close();
                    return;
                }
                add--;
                String insts = virtualMemory[add][0] + "" + virtualMemory[add][1];
                if (insts.equals("GD") || insts.equals("PD") || insts.equals("H")) {

                    mos(virtualMemory[add], prv_line);
                } else if (insts.equals("LR") || insts.equals("SR") || insts.equals("CR") ||
                        insts.equals("BT")) {
                    user_program(virtualMemory[add]);
                }
            }
            return;
        }

        int start_add = addressMap(add);
        // System.out.println(start_add);
        if (start_add < 0 && !inst.equals("SR")) {
            load();
            PrintWriter writer = new PrintWriter("output.txt");
            writer.print("");
            writer.close();
            throw new OSError("INVALID PAGE FAULT");
        } else if (start_add < 0 && inst.equals("SR")) {

            int randNo = getRandom(0, 30);

            while (!uniqueValues.isEmpty() && uniqueValues.contains(randNo)) {
                randNo = getRandom(0, 30);
            }
            uniqueValues.add(randNo);
            start_add = -start_add;
            memory[start_add][2] = (char) ((randNo / 10) + '0');
            memory[start_add][3] = (char) ((randNo % 10) + '0');
            start_add = addressMap(add);
        }
        start_add = start_add - start_add % 10;

        if (inst.equals("LR")) {

            cpu.r = memory[start_add];

        } else if (inst.equals("SR")) {

            memory[start_add] = cpu.r;

        } else if (inst.equals("CR")) {

            if (cpu.r == memory[start_add])
                cpu.toggle = true;
            else
                cpu.toggle = false;

        } else {

            System.out.println(inst);
        }

    }

    private static void mos(char[] instruction, String buffer) throws IOException, OSError {
        cpu.ir = instruction;

        String inst = cpu.ir[0] + "" + cpu.ir[1];

        if ((cpu.ir[0] != 'H') && ((cpu.ir[2] > 58 || cpu.ir[2] < 48) || (cpu.ir[3] > 58 || cpu.ir[3] < 48))) {
            System.out.println("OPERAND ERROR");
            load();
            PrintWriter writer = new PrintWriter("output.txt");
            writer.print("");
            writer.close();
            return;
        }

        if (inst.charAt(0) == 'H') {
            FileWriter fr = new FileWriter("output.txt", true);
            fr.write("\n\n");
            fr.close();
            return;
        }
        int add = (cpu.ir[2] - '0') * 10 + (cpu.ir[3] - '0');
        int start_add = addressMap(add);
        // System.out.println(start_add);
        if (start_add < 0 && !inst.equals("GD")) {
            load();
            PrintWriter writer = new PrintWriter("output.txt");
            writer.print("");
            writer.close();
            throw new OSError("INVALID PAGE FAULT");
        } else if (start_add < 0 && inst.equals("GD")) {

            int randNo = getRandom(0, 30);

            while (!uniqueValues.isEmpty() && uniqueValues.contains(randNo)) {
                randNo = getRandom(0, 30);
            }
            uniqueValues.add(randNo);
            start_add = -start_add;
            memory[start_add][2] = (char) ((randNo / 10) + '0');
            memory[start_add][3] = (char) ((randNo % 10) + '0');
            start_add = addressMap(add);
        }
        start_add = start_add - start_add % 10;
        // System.out.println(start_add);

        if (inst.equals("GD")) {
            int k = 0;
            if (buffer.contains(" "))
                buffer = buffer.replace(' ', '@');
            for (int i = start_add; i < start_add + 10; i++) {

                if (buffer.substring(k).length() >= 4) {
                    memory[i] = buffer.substring(k, k + 4).toCharArray();
                    k += 4;
                } else {
                    memory[i] = buffer.substring(k).toCharArray();
                    break;
                }
            }

            prv_line = buffer;
        } else if (inst.equals("PD")) {

            StringBuilder sb = new StringBuilder();
            for (int i = start_add; i < start_add + 10; i++) {

                sb.append(memory[i]);
            }

            FileWriter fr = new FileWriter("output.txt", true);

            String s = sb.toString().replace('@', ' ');

            fr.write(s.replace('*', ' '));
            fr.close();
        }
    }

    static int row;

    private static void instnSet(String instruction) throws FileNotFoundException {
        int i = 0;
        while (i < instruction.length()) {
            if (instruction.charAt(i) == 'H') {
                virtualMemory[row][0] = instruction.charAt(i);
                i++;
            } else {
                try {
                    virtualMemory[row] = instruction.substring(i, i + 4).toCharArray();
                    i += 4;
                } catch (StringIndexOutOfBoundsException e) {
                    // TODO: handle exception
                    System.out.println("NO OPERAND PRESENT");
                    load();
                    return;
                }

            }
            row++;
        }
    }

    public static void load() throws FileNotFoundException {
        PTR = getRandom(0, 291);
        uniqueValues = new HashSet<>();
        cpu = new CPU();
        memory = new char[300][4];
        virtualMemory = new char[100][4];
        for (char[] row : virtualMemory)
            Arrays.fill(row, '*');
        for (char[] row : memory)
            Arrays.fill(row, '*');

    }

    public static int getRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public static int addressMap(int virtualAdress) {
        int PTE = PTR + (virtualAdress) / 10;
        if (memory[PTE][2] == '*' && memory[PTE][3] == '*') {
            return -(PTE);
        } else {
            int randValue = (memory[PTE][2] - '0') * 10 + memory[PTE][3] - '0';
            return randValue * 10 + (virtualAdress) % 10;
        }
    }

}
