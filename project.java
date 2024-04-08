
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
public class project {
    public static HashMap<Integer, String> map = new HashMap<>();
    public static Map<String, String> rTypeMap = new HashMap<>();
    public static Map<String, String> iTypeMap = new HashMap<>();
    public static Map<String, String> jTypeMap = new HashMap<>();
    public static Map<String, String> floatTypeMap = new HashMap<>();
    public static List<String> standardRType = Arrays.asList("ADD", "ADDU", "SUB", "SUBU", "AND", "OR", "XOR", "NOR", "SLT");
    public static List<String> shiftRType = Arrays.asList("SLL", "SRL", "SRA");
    public static List<String> iFormat = Arrays.asList("BEQ", "BNE", "ADDI", "ADDIU", "SLTI", "LW", "SW");
    public static List<String> branchImmediate = Arrays.asList("BGEZ", "BGTZ", "BLEZ", "BLTZ");
    public static List<String> floatOpps = Arrays.asList("DIV.S", "MUL.S", "SUB.S", "ADD.S", "MOV.S");
    public static List<String> floatStore = Arrays.asList("LWC1", "SWC1");
    public static List<String> jType = Arrays.asList("J", "JAL");
    public static List<String> iFormImmediate = Arrays.asList("ADDI", "ADDIU", "SLTI");
    public static List<String> iFormMemAccess = Arrays.asList("LW", "SW");
    public static List<String> iFormBranch = Arrays.asList("BEQ", "BNE");

    static{
        //For r type we take their funct code 
        rTypeMap.put("100000", "ADD");
        rTypeMap.put("100001", "ADDU");
        rTypeMap.put("100010", "SUB");
        rTypeMap.put("100011", "SUBU");
        rTypeMap.put("100100", "AND");
        rTypeMap.put("100101", "OR");
        rTypeMap.put("100110", "XOR");
        rTypeMap.put("100111", "NOR");
        rTypeMap.put("101010", "SLT");
        rTypeMap.put("000000", "SLL"); // NOP is a special case of SLL
        rTypeMap.put("000010", "SRL");
        rTypeMap.put("000011", "SRA");
        rTypeMap.put("001000", "JR");
        rTypeMap.put("001101", "BREAK");
        iTypeMap.put("000100", "BEQ");
        iTypeMap.put("000101", "BNE");
        iTypeMap.put("001000", "ADDI");
        iTypeMap.put("001001", "ADDIU");
        iTypeMap.put("001010", "SLTI");
        iTypeMap.put("100011", "LW");
        iTypeMap.put("101011", "SW");
        iTypeMap.put("000001", "BGEZ"); // Needs special handling to distinguish BGEZ from BLTZ
        iTypeMap.put("000111", "BGTZ");
        iTypeMap.put("000110", "BLEZ");
        iTypeMap.put("110001", "LWC1");
        iTypeMap.put("111001", "SWC1");
        iTypeMap.put("000010", "J");
        iTypeMap.put("000011", "JAL");
        floatTypeMap.put("000000", "ADD.S");
        floatTypeMap.put("000011", "DIV.S");
        floatTypeMap.put("000110", "MOV.S");
        floatTypeMap.put("000001", "SUB.S");
        floatTypeMap.put("000010", "MUL.S");


    }


    public static void readFile(String file, String outputFile) throws IOException{
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {
            //New File
            File f = new File(file);
            //Open the Scanner
            Scanner s = new Scanner(f);
            //Read in all the data in the file line by line until there are no more lines
            int address = 496;
            boolean breakFound = false;
            while (s.hasNextLine()){
                String rs = "";
                String rt = "";
                String rd = "";
                int shamt;
                String mipsFunct = "";
                String instruction = "";
                //Takes in the data for the line and turns it into string
                String line = s.nextLine();

                //Trim all whitespace from the line
                line = line.trim();
                if (line.isEmpty()){
                    writer.newLine();
                    continue;
                }
                //Here we split the binary number into its 6 parts to format for the output file
                String first = line.substring(0,6);
                String second = line.substring(6, 11);
                String third = line.substring(11, 16);
                String fourth = line.substring(16, 21);
                String fifth = line.substring(21, 26);
                String sixth = line.substring(26, 32);
                //place the function into our mipsFunct Variable
                mipsFunct = functIdentifier(line);
                //Here we check to see if we reached the end of the function
                if (mipsFunct == "BREAK"){
                    //If we do write in the Break line and set breakFound to true
                    writer.write(first + " " + second + " " + third + " " + fourth + " " + fifth + " " + sixth + "   " + address + " " + mipsFunct);
                    writer.newLine();
                    breakFound = true;
                    address += 4;
                    continue;
                }
                //If break found is true we print the address and the number 0 next to it
                if (breakFound == true){
                    writer.write(first + " " + second + " " + third + " " + fourth + " " + fifth + " " + sixth + "   " + address + " " + "0");
                    writer.newLine();
                }
                else {
                    if (line.equals("00000000000000000000000000000000")){
                        writer.write(first + " " + second + " " + third + " " + fourth + " " + fifth + " " + sixth + "   " + address + " " + "NOP");
                        writer.newLine();
                    }
                    //If break hasnt been found we create a for loop to format for the standard R types and write them in
                    for (int i = 0; i < standardRType.size(); ++i){
                        if (mipsFunct.equals(standardRType.get(i))){
                            rs = line.substring(6, 11);
                            rt = line.substring(11, 16);
                            rd = line.substring(16, 21);
                            rs = getRegisterName(rs);
                            rt = getRegisterName(rt);
                            rd = getRegisterName(rd);
                            writer.write(first + " " + second + " " + third + " " + fourth + " " + fifth + " " + sixth + "   " + address + " " + mipsFunct + " " + rd + ", " + rs + ", " + rt);
                            writer.newLine();
                        }
                    }
                    for (int i = 0; i < shiftRType.size(); ++i){
                        //Here we check if the function is equal to the function name in the Shift Type
                        if (mipsFunct.equals(shiftRType.get(i))){
                            rt = line.substring(11, 16);
                            rd = line.substring(16, 21);
                            String shamtStr = line.substring(21, 26);
                            rt = getRegisterName(rt);
                            rd = getRegisterName(rd);
                            shamt = Integer.parseInt(shamtStr, 2);
                            //Put it back together here
                            writer.write(first + " " + second + " " + third + " " + fourth + " " + fifth + " " + sixth + "   " + address + " " + mipsFunct + " " + rd + ", " + rt + ", #" + shamt);
                            writer.newLine();
                        }
                    }
                    //Here we check for a special case of Jump and Return
                    if (mipsFunct.equals("JR")){
                        rs = line.substring(6, 11);
                        rs = getRegisterName(rs);
                        writer.write(first + " " + second + " " + third + " " + fourth + " " + fifth + " " + sixth + "   " + address + " " + mipsFunct + " " + rs);
                        writer.newLine();
                    }
                    //Here we check the iformat functions
                    for (int i = 0; i < iFormat.size(); ++i){
                        if (mipsFunct.equals(iFormat.get(i))){
                             //Here we check for immediate and print them out differently 
                            for (int j = 0; j < iFormImmediate.size(); ++j){
                                if (mipsFunct.equals(iFormImmediate.get(j))){
                                    rs = line.substring(6, 11);
                                    rt = line.substring(11, 16);
                                    instruction = line.substring(16, 32);
                                    rs = getRegisterName(rs);
                                    rt = getRegisterName(rt);
                                    int immediateValue = Integer.parseInt(instruction, 2);
                                    //We shift the number 16 bits over and then shift them back to get the real number
                                    int signValue = (immediateValue << 16) >> 16;
                                    //If the number is negative then we print it out with a negative sign and if its positive we print it with no sign
                                    boolean isNegative = signValue < 0;
                                    //Here we format it for if its negative
                                    if (isNegative == true){
                                        writer.write(first + " " + second + " " + third + " " + fourth + " " + fifth + " " + sixth + "   " + address + " " + mipsFunct + " " + rt + ", " + rs + ", #" + signValue);
                                        writer.newLine();
                                    }
                                    //Here we format it if its positve
                                    else {
                                        writer.write(first + " " + second + " " + third + " " + fourth + " " + fifth + " " + sixth + "   " + address + " " + mipsFunct + " " + rt + ", " + rs + ", #" + signValue);
                                        writer.newLine();
                                    }   
                                }
                            }
                            for (int h = 0; h < iFormMemAccess.size(); ++h){
                                if (mipsFunct.equals(iFormMemAccess.get(h))){
                                    //Here we get the rt and the base for the mem access
                                    rt = line.substring(11, 16);
                                    String baseStr = line.substring(6, 11);
                                    String offsetStr = line.substring(16, 32);
                                    baseStr = getRegisterName(baseStr);
                                    int offset = Integer.parseInt(offsetStr, 2);
                                    rt = getRegisterName(rt);
                                    writer.write(first + " " + second + " " + third + " " + fourth + " " + fifth + " " + sixth + "   " + address + " " + mipsFunct + " " + rt + ", " + offset + "(" + baseStr + ")");
                                    writer.newLine();  
                                }
                            }
                            for (int b = 0; b < iFormBranch.size(); ++b){
                                if (mipsFunct.equals(iFormBranch.get(b))){
                                    //Here we get the i type branches
                                    rs = line.substring(6, 11);
                                    rt = line.substring(11, 16);
                                    String offsetStr = line.substring(16, 32);
                                    rs = getRegisterName(rs);
                                    rt = getRegisterName(rt);
                                    int offset = Integer.parseInt(offsetStr, 2);
                                    offset = offset << 2;
                                    writer.write(first + " " + second + " " + third + " " + fourth + " " + fifth + " " + sixth + "   " + address + " " + mipsFunct + " " + rs + ", " + rt + ", #" + offset);
                                    writer.newLine();
                                }
                            }
                           
                        }
                    }
                    //Here we check if its a mips function from my branch immediate list
                    for (int i = 0; i < branchImmediate.size(); ++i){
                        if (mipsFunct.equals(branchImmediate.get(i))){
                            if (mipsFunct.equals("BGEZ")){
                                //Here we get the BGEZ code as its the same funct code and then we check them both manually
                                rt = line.substring(11, 16);
                                rs = line.substring(6,11);
                                rs = getRegisterName(rs);
                                String offset = line.substring(16, 32);
                                int offsetInt = Integer.parseInt(offset, 2);
                                //If its rt is equal to the condition it is BGEZ
                                if (rt.equals("00001")){
                                    //Here we check to see if its BGEZ and print it out
                                    writer.write(first + " " + second + " " + third + " " + fourth + " " + fifth + " " + sixth + "   " + address + " BGEZ " + rs + ", #" + offsetInt);
                                    writer.newLine();
                                }
                                //If its not its BLTZ
                                else{
                                    writer.write(first + " " + second + " " + third + " " + fourth + " " + fifth + " " + sixth + "   " + address + " BLTZ " + rs + ", #" + offsetInt);
                                    writer.newLine();
                                }
                            }
                            //If its neither of those we format it the same for the rest of them
                            rs = line.substring(6, 11);
                            String offsetStr = line.substring(16, 32);
                            rs = getRegisterName(rs);
                            int offset = Integer.parseInt(offsetStr);
                            writer.write(first + " " + second + " " + third + " " + fourth + " " + fifth + " " + sixth + "   " + address + " " + mipsFunct + " " + rs + ", #" + offset);
                            writer.newLine();
                        }
                    }
                    //Here we handle the writing for float store operators
                    for (int i = 0; i < floatStore.size(); ++i){
                        if (mipsFunct.equals(floatStore.get(i))){
                            //If its a store we get the offset base and register 
                            String base = line.substring(6, 11);
                            String ft = line.substring(11, 16);
                            String offsetStr = line.substring(16, 32);
                            //for base and ft we get their register name
                            base = getRegisterName(base);
                            ft = getRegisterName(ft);
                            //For the offset we parse the integer to get the offset
                            int offset = Integer.parseInt(offsetStr, 2);
                            writer.write(first + " " + second + " " + third + " " + fourth + " " + fifth + " " + sixth + "   " + address + " " + mipsFunct + " " + ft + " " + offset + "(" + base + ")");
                            writer.newLine();
                        }
                    }
                    //Here we handle the writing for float ops 
                    for (int i = 0; i < floatOpps.size(); ++i){
                        if (mipsFunct.equals(floatOpps.get(i))){
                            //If its a float opperation then we get the different registers through substrings
                            String ft = line.substring(11, 16);
                            String fs = line.substring(16, 21);
                            String fd = line.substring(21, 26);
                            //Get their register names
                            ft = getRegisterName(ft);
                            fs = getRegisterName(fs);
                            fd = getRegisterName(fd);
                            writer.write(first + " " + second + " " + third + " " + fourth + " " + fifth + " " + sixth + "   " + address + " " + mipsFunct + " " + fd + ", " + fs + ", " + ft);
                            writer.newLine();
                        }
                    }
                    //Here we handle the writing for j type
                    for (int i = 0; i < jType.size(); ++i){
                        if (mipsFunct.equals(jType.get(i))){
                            //If it is equal we get the target substring
                            String target = line.substring(6, 32);
                            //convert it into an integer
                            int targetInt = Integer.parseInt(target, 2);
                            //Shift left by 2 to get the address
                            targetInt = (targetInt << 2);
                            writer.write(first + " " + second + " " + third + " " + fourth + " " + fifth + " " + sixth + "   " + address + " " + mipsFunct + "  #" + targetInt);
                            writer.newLine();
                        }
                    }
                }
                //Take in and add each number as a String
                address = address + 4;
            }
            s.close();
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     * 
     * @param binary This is the string of the binary representation of the mips code
     * @param decodedString This is where we will store what function it is in this string
     */
    public static String functIdentifier(String binary){
        //These are the binary representations for the opcode identifier
        String opcode = binary.substring(0,6);
        //These are the binary representations for the function identifier
        String funct = binary.substring(26, 32);
        //This will check if the op code is an r type as all r types has 000000 opcode identifiers
        if ("000000".equals(opcode)){
            //This is to distinguish between NOP and SLL
            if ("000000".equals(funct)){
                String rt = binary.substring(16, 21);
                if ("00000".equals(rt)){
                    return "NOP";
                }
                else {
                    return "SLL";
                }
            }
            else{
                //We add the function name to the decoded string
                return rTypeMap.get(funct);
            }
            
        }
        //Check if its a co processor 1 function then if it is we get floating point values 
        else if ("010001".equals(opcode)){
            //This will get the float function based on the last 6 bits
            return floatTypeMap.get(funct);
        }
        //If its not an r type then we know its either a j type or an i type
        else  {
            //If its 000001 then we know its a special case of either BGEZ or BLTZ
            if ("000001".equals(opcode)){
                //We get the rt fields to distinguish them
                String rt = binary.substring(16, 21);
                //If this is true then we add BGEZ to our decoded string
                if ("00001".equals(rt)){
                    return "BGEZ";
                }
                //If this is true then we get BLTZ and add it to our decoded string
                if ("00000".equals(rt)){
                    return "BLTZ";
                }
            }
            //If its not a special case then we just get the function and add to our decoded string
            return iTypeMap.get(opcode);
        }  
        //Here we check the float type
    }
    /**
     * This function just gets the register number thats it
     * @param binary
     * @return
     */
    public static String getRegisterName(String binary){
        //We parse the int and then get the register number
        int rgeisterNumber = Integer.parseInt(binary, 2);
        return "R" + rgeisterNumber;
    }






    public static void main(String[] args) throws IOException{
        System.out.println("Please Select an Input File");
        Scanner scnr = new Scanner(System.in);
        String file = scnr.nextLine();
        System.out.println("Please Select an Output File");
        String outputFile = scnr.nextLine();
        scnr.close();
        readFile(file, outputFile);
        System.out.println("Done!");
    }

}
