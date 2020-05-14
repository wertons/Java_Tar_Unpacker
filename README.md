# TAR File unpacker
This is a simple Java program to unpack .tar files. It loads the TAR file in runtime in order to unpack it so the program has to be used with care so as to not overload your virtual memory. The processing time is also not very efficient so it really shouldn't be used for massive files or batch operations.

It consists of 2 classes 5 methods and in the main it has a prebuilt simple console in order to use it.

The two classes are one for the program itself and a support class called Tarfile which is used to load files in runtime memory.

Here is a quick rundown of the methods:

- **Tar**: This is the constructor, its input is the filepath of the file you want to unpack. When run it will try to store the file onto a FileInputStream. If it succesfully finds the file it prints "File found successfully" in the case that it has an error while reading the file it prints "File not found"
- **expand**: The expand method is the one that loads the file in virtual memory and structures it to facilitate the use of the data. It starts by loading the FileInputStream into a byte array. Then it loops in order to find all files. The TAR file format is divided in blocks of 512 bytes and the first of these blocks is always a header, so the loop checks if there are available files by trying to read the first 512 from the cursor. The cursor is simply a variable which starts at zero. If it can read a header it creates a custom **TarFile** and stores it in there.
Now using the **setSize** method we extract the size of the file itself. Using the size we now extract the file itself from the Tar and store in the previously created **TarFile**. From the header we also read the first 100 bytes in order to get the file name. With the name and the contents we finish adding the information to the **TarFile** and we advance the cursor by 512 + the file size, this makes it so the byte on the cursor is either null or the next file. Then the loop resets. Once done we print "Successfully loaded".

- **setSize**: This method reads the header in order to find the size of the file. The size is stored between the 124th-136th bytes and is stored in octal format. To extract it we simply do a mathematical conversion. It should also be noted that the blocks of the file are always 512, so usually the last block has empty space, we must add these spaces so the cursor works properly, although these unnecesary bytes must not be added to the file itself.

- **getBytes**: Tries to return the content of a file in the form of a byte array, its input is the name of a file. It only works if the Tar is loaded in memory. If it cant find the file it prints "The chosen file cannot be found".

- **list**: This method returns a String array containing the names of all the files in the loaded Tar.
