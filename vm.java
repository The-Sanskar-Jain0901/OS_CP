
import java.io.*;
import java.util.Arrays;

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
    static String prv_line = "";
    static CPU cpu;

    public static void main(String[] args) {

        File file = new File(
                "E:\\Study Material\\OS_CP\\src\\input.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String buffer;
            int line_no = 0;
            while ((buffer = br.readLine()) != null) {

                if (buffer.substring(0, 4).equals("$AMJ")) {

                    load();
                } else if (buffer.substring(0, 4).equals("$DTA")) {
                    start_exec(line_no + 1);
                    for (int i = 0; i < 90; i++) {
                        System.out.println(memory[i]);
                    }

                } else if (buffer.substring(0, 4).equals("$END")) {

                } else {
                    instnSet(buffer);
                }
                line_no++;
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    // performing instructions on data cards
    private static void start_exec(int line_no) throws FileNotFoundException, IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("E:\\Study Material\\OS_CP\\src\\input.txt"))) {
            // skipping n lines
            for (int i = 0; i < line_no; i++)
                br.readLine();
            for (int i = 0; i < 10; i++) {

                String line = "";
                String inst = memory[i][0] + "" + memory[i][1];
                if (inst.equals("GD")) {
                    if ((line = br.readLine()) != null && (line) != "$END") {
                        mos(memory[i], line);
                    }
                } else if (inst.equals("PD")) {
                    mos(memory[i], line);
                } else if (inst.substring(0, 1).equals("H")) {
                    mos(memory[i], line);
                    i = 10;
                } else if (inst.equals("LR") || inst.equals("SR") || inst.equals("CR")) {
                    user_program(memory[i]);
                } else if (inst.equals("BT")) {
                    if (cpu.toggle)
                        i = (memory[i][2] - '0') * 10 + memory[i][3] - '0';
                    user_program(memory[i]);

                }

            }
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private static void user_program(char[] instruction) throws IOException {
        // lr,sr,cr,bt
        cpu.ir = instruction;
        String inst = cpu.ir[0] + "" + cpu.ir[1];
        // System.out.println(inst);
        if (inst.equals("LR")) {
            int memory_loc = (cpu.ir[2] - '0') * 10 + (cpu.ir[3] - '0');

            cpu.r = memory[memory_loc];

        } else if (inst.equals("SR")) {
            int memory_loc = (cpu.ir[2] - '0') * 10 + (cpu.ir[3] - '0');
            memory[memory_loc] = cpu.r;

        } else if (inst.equals("CR")) {
            int memory_loc = (cpu.ir[2] - '0') * 10 + (cpu.ir[3] - '0');
            if (cpu.r == memory[memory_loc])
                cpu.toggle = true;
            else
                cpu.toggle = false;

        } else if (inst.equals("BT")) {
            int memory_loc = (cpu.ir[2] - '0') * 10 + (cpu.ir[3] - '0');
            if (cpu.toggle) {
                String insts = memory[memory_loc][0] + "" + memory[memory_loc][1];
                if (insts.equals("GD") || insts.equals("PD") || insts.equals("H")) {
                    mos(memory[memory_loc], prv_line);
                } else if (insts.equals("LR") || insts.equals("SR") || insts.equals("CR") || insts.equals("BT")) {
                    user_program(memory[memory_loc]);
                }
            }

        } else {

            System.out.println(inst);
        }

    }

    private static void mos(char[] instruction, String buffer) throws IOException {
        cpu.ir = instruction;
        String inst = cpu.ir[0] + "" + cpu.ir[1];
        // System.out.println(instruction);
        if (inst.equals("GD")) {
            int start_add = (cpu.ir[2] - '0') * 10;
            int k = 0;
            if (buffer.contains(" "))
                buffer = buffer.replace(' ', '*');
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
            int start_add = (cpu.ir[2] - '0') * 10;
            StringBuilder sb = new StringBuilder();
            for (int i = start_add; i < start_add + 9; i++) {

                sb.append(memory[i]);
            }
            // File file = new File();
            FileWriter fr = new FileWriter("output.txt", true);

            fr.write(sb.toString().replace('*', ' ') + "\n");
            fr.close();
        } else if (inst.substring(0, 1).equals("H")) {

            FileWriter fr = new FileWriter("output.txt", true);
            fr.write("\n\n");
            fr.close();

        }
    }

    private static void instnSet(String instruction) {
        int i = 0;
        int j = 0;
        while (i < instruction.length() - 3) {

            if (instruction.charAt(i) == 'H') {
                memory[j][0] = 'H';
                i++;
            } else {
                memory[j] = instruction.substring(i, i + 4).toCharArray();

                i = i + 4;
            }

            j++;
        }
        memory[j][0] = 'H';

    }

    public static void load() throws FileNotFoundException {

        cpu = new CPU();
        memory = new char[100][4];
        for (char[] row : memory)
            Arrays.fill(row, '*');
        // PrintWriter writer = new PrintWriter("output.txt");
        // writer.print("");
        // writer.close();
    }
}
