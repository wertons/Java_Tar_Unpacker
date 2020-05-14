import org.junit.Ignore;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class Tar {
    List<TarFile> files = new ArrayList<TarFile>();
    String filePath;
    byte[] fullFile;
    byte[] file;
    int blockSize = 512;
    int currentByte = 0;
    byte[] header = new byte[blockSize];
    int size;
    int fillSize;
    boolean foundAll = false;
    InputStream reader;
    boolean loaded;
    boolean found = false;

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        boolean running = true;
        while (running) {
            //Console TAR program
            System.out.println("Enter the path to the TAR you want to search:");
            String userInput = sc.next();
            Tar loadedTar = new Tar(userInput);
            if (loadedTar.found) {
                String tarPath = userInput;
                boolean inProOp = true;
                while (inProOp) {
                    System.out.println("Press key for the following actions: ");
                    System.out.println("1-Load\n2-List\n3-Extract\n4-Change File");
                    int userChoice = sc.nextInt();
                    switch (userChoice) {
                        case 1:
                            if (!loadedTar.loaded) {
                                loadedTar.expand();
                            } else {
                                System.out.println("File is already loaded in memory");
                            }
                            break;
                        case 2:
                            if (!loadedTar.loaded) {
                                System.out.println("File is not loaded in memory, please load it first");
                            } else {
                                String[] listOut = loadedTar.list();
                                System.out.println("\nFound " + listOut.length + " files");
                                System.out.println("----------------------------");
                                for (int i = 0; i < listOut.length; i++) {
                                    System.out.println("-" + listOut[i]);
                                }
                                System.out.println("----------------------------\n");
                            }
                            break;
                        case 3:
                            if (!loadedTar.loaded) {
                                System.out.println("File is not loaded in memory, please load it first");
                            } else {
                                System.out.println("What file do you want to extract");
                                String[] listOut = loadedTar.list();
                                System.out.println("----------------------------");
                                int i = 0;
                                for (i = 0; i < listOut.length; i++) {
                                    System.out.println(i+1 + "-" + listOut[i]);
                                }
                                System.out.println((i + 1) + "-Cancel extraction");
                                System.out.println("----------------------------");
                                System.out.println("Type the number to choose\n");
                                userChoice = sc.nextInt();
                                if (userChoice != i + 1) {
                                    TarFile currentFile = loadedTar.files.get(userChoice);
                                    System.out.println("Extracting " + currentFile.fileName);
                                    System.out.println("\nWhat folder do you want to extract to? (Folder paths end in / or \\)");
                                    System.out.println("Enter \"local\" to extract in same folder");
                                    userInput = sc.next();
                                    if (userInput.equals("local")) {
                                        userInput = tarPath.replace("\\\\(?:.(?!\\\\))+$", "");
                                    }
                                    try {
                                        File file = new File(userInput + currentFile.fileName);
                                        OutputStream os = new FileOutputStream(file);
                                        os.write(currentFile.content);
                                        System.out.println("Successfully extracted");
                                        os.close();

                                    } catch (Exception ignore) {
                                        System.out.println("Could not complete operation");
                                    }
                                }
                            }
                            break;
                        case 4:
                            inProOp = false;
                            break;
                    }
                }
            }
        }
    }

    // Constructor
    public Tar(String filepath) throws IOException {
        this.filePath = filepath;
        try {
            reader = new FileInputStream(filepath);
            System.out.println("File found successfully!");
            found = true;
        } catch (IOException e) {
            System.out.println("File not found!");
        }
        this.loaded = false;
    }

    void setSize(byte[] header) {
        String size = "";
        for (int i = 0; i < 11; i++) {
            size += header[124 + i] - 48;
        }
        int tmpsize = Integer.parseInt(size, 8);
        this.size = tmpsize;
        while (tmpsize % 512 != 0) {
            tmpsize++;
        }
        this.fillSize = tmpsize;
    }

    // Torna un array amb la llista de fitxers que hi ha dins el TAR
    public String[] list() {
        String[] result = new String[files.size()];
        for (int i = 0; i < files.size(); i++) {
            result[i] = files.get(i).fileName;
        }
        return result;
    }

    // Torna un array de bytes amb el contingut del fitxer que té per nom
    // igual a l'String «name» que passem per paràmetre
    public byte[] getBytes(String name) {
        for (TarFile tarFile : files) {
            if (tarFile.fileName.equals(name)) {
                return tarFile.content;
            }
        }
        return null;
    }

    // Expandeix el fitxer TAR dins la memòria
    public void expand() throws IOException {

        fullFile = reader.readAllBytes();

        while (!foundAll) {
            TarFile tmpFile = new TarFile();
            //Extract the header from the first block
            try {
                header = Arrays.copyOfRange(fullFile, currentByte, currentByte + blockSize);
                currentByte += 512;
                //Get the size of the file from the header block
                setSize(header);
                tmpFile.setHeader(header);
                //Get the file itself
                file = new byte[size];
                file = Arrays.copyOfRange(fullFile, currentByte, currentByte + size);
                currentByte += fillSize;
                tmpFile.setContent(file);
                String fileName = "";
                //Extract the name from the first 100 bytes of the header
                for (int i = 0; i < 100; i++) {
                    byte tmpByte = header[i];
                    if (tmpByte != 0) {
                        fileName += Character.toString((char) tmpByte);
                    } else {
                        break;
                    }
                }
                tmpFile.setFileName(fileName);
                tmpFile.build();
                files.add(tmpFile);
            } catch (Exception e) {
                System.out.println("Found all files");
                foundAll = true;
            }
        }
        System.out.println("Successfully loaded:" + filePath);
        this.loaded = true;
    }
}

class TarFile {
    String fileName;
    String name;
    String fileType;
    int size;
    byte[] header;
    byte[] content;

    TarFile() {
    }

    void build() {
        size = content.length + header.length;
        StringBuilder separate = new StringBuilder();
        separate.append(fileName);
        int tmp = separate.indexOf(".");
        name = separate.substring(0, tmp - 1);
        fileType = separate.substring(tmp + 1);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setHeader(byte[] header) {
        this.header = header;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
