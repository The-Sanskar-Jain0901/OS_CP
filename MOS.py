package CP.Phase1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

    static CPU cpu;

    public static void main(String[] args) {
        File file = new File(
                "D:\\OS\\CP\\Phase1\\input.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String buffer;
            int line_no = 0;
            while ((buffer = br.readLine()) != null) {

                if (buffer.substring(0, 4).equals("$AMJ")) {
                    cpu = new CPU();
                    memory = new char[100][4];
                    for (char[] row : memory)
                        Arrays.fill(row, '*');

                } else if (buffer.substring(0, 4).equals("$DTA")) {
                    start_exec(line_no + 1);
                    for (int i = 0; i < 30; i++)
                        System.out.println(memory[i]);
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
        try (BufferedReader br = new BufferedReader(new FileReader("D:\\OS\\CP\\Phase1\\input.txt"))) {
            // skipping n lines
            for (int i = 0; i < line_no; i++)
                br.readLine();
            for (int i = 0; i < 10; i++) {

                String line;
                if ((line = br.readLine()) != null) {

                    String inst = memory[i][0] + "" + memory[i][1];
                    // System.out.println(inst);
                    if (inst.equals("gd") || inst.equals("pd") || inst.equals("H")) {
                        mos(memory[i], line);
                    } else {
                        user_program();
                    }
                }
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    private static void user_program() {
    }

    private static void mos(char[] instruction, String buffer) {
        cpu.ir = instruction;
        String inst = instruction[0] + "" + instruction[1];
        if (inst.equals("gd")) {
            int start_add = (instruction[2] - '0') * 10;
            int k = 0;
            for (int i = start_add; i < start_add + 10; i++) {
                if (buffer.substring(k).length() >= 4) {
                    memory[i] = buffer.substring(k, k + 4).toCharArray();
                    k += 4;
                } else {
                    memory[i] = buffer.substring(k).toCharArray();
                    break;
                }
            }

        } else if (instruction.toString().substring(0, 2).equals("pd")) {

        } else if (instruction.toString().substring(0, 2).equals("H")) {

        }
    }

    private static void instnSet(String instruction) {
        int i = 0;
        int j = 0;
        while (i < instruction.length() - 3) {

            if (instruction.charAt(i) == 'H') {
                memory[j][0] = instruction.charAt(i);

                i++;
            }

            else {
                memory[j] = instruction.substring(i, i + 4).toCharArray();

                i = i + 4;
            }
            j++;
        }

    }
}
