package sg.edu.nus.comp.cs4218.impl;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.Shell;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.CommandBuilder;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class ShellImpl implements Shell {

    /**
     * Main method for the Shell Interpreter program.
     *
     * @param args List of strings arguments, unused.
     */
    public static void main(String... args) {
        InputStreamReader inputStreamReader = new InputStreamReader(System.in); //NOPMD - suppressed CloseResource - System Close
        BufferedReader reader = new BufferedReader(inputStreamReader); //NOPMD - suppressed CloseResource - System Close
        Shell shell = new ShellImpl();

        while (true) {
            try {
                String commandString;
                System.out.print("> ");
                try {
                    commandString = reader.readLine();
                } catch (IOException e) {
                    inputStreamReader.close();
                    reader.close();
                    return; // Streams are closed, terminate process
                }

                if (!StringUtils.isBlank(commandString)) {
                    shell.parseAndEvaluate(commandString, System.out);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public void parseAndEvaluate(String commandString, OutputStream stdout)
            throws AbstractApplicationException, ShellException, FileNotFoundException {
        Command command = CommandBuilder.parseCommand(commandString, new ApplicationRunner());
        command.evaluate(System.in, stdout);
    }
}
