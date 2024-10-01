package org.example;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

public class Main{
    public static String start_path = "test_arh/";
    public static String script_name = "";
    public static String current_path = start_path;
    public static ArrayList<String> directories = new ArrayList<>();
    public static ArrayList<String> files = new ArrayList<>();
    public static int path_count(String path) {
        int out = 0;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/')
                out++;
        }
        return out;
    }

    public static boolean checkDir(String path, Boolean isDir) { // функция для проверки, находится ли директория/файл в текущей папке
        if (path.startsWith(current_path)) {
            if (isDir) {
                if (path_count(current_path) == path_count(path) - 1) {
                    return true;
                }
                else return false;
            }
            else {
                if (path_count(current_path) == path_count(path)) {
                    return true;
                }
                else return false;
            }
        }
        else return false;
    }
    public static ArrayList<String> unTar(String path) { // распаковка .tar архива
        directories.clear();
        files.clear();
        ArrayList<String> out = new ArrayList<>();
        try {
            File inputTarFile = new File(path);

            try (InputStream fileInputStream = new FileInputStream(inputTarFile);
                 InputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                 //InputStream gzipInputStream = new GZIPInputStream(bufferedInputStream);
                 // если tar архив сжат, то у него ещё расширение .gz, и тогда надо расскомментить верхнюю строчку
                 // и передать в нижнюю не bufferedInputStream, а gzipInputStream
                 TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(bufferedInputStream))
            {

                TarArchiveEntry entry;

                while ((entry = tarArchiveInputStream.getNextTarEntry()) != null) {
                    Boolean b = entry.isDirectory();
                    if (checkDir(entry.getName(), b)) {
                        out.add(entry.getName());
                        if (entry.getName().contains(current_path)) {
                            if (b) directories.add(entry.getName().substring(entry.getName().indexOf(current_path) + current_path.length()));
                            else files.add(entry.getName().substring(entry.getName().indexOf(current_path) + current_path.length()));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }

    public static ArrayList<String> read_script(String name) {
        ArrayList<String> out = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(name));
            String line = "";
            while ( (line = bufferedReader.readLine()) != null) {
                out.add("> " + line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    public static ArrayList<String> read_file(String name, int k) {
        ArrayList<String> out = new ArrayList<>();
        try {
            File inputTarFile = new File("src/test_arh.tar");
            try (InputStream fileInputStream = new FileInputStream(inputTarFile);
                 InputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                 TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(bufferedInputStream))
            {
                TarArchiveEntry entry;
                while ((entry = tarArchiveInputStream.getNextTarEntry()) != null) {
                    if (entry.isFile() && entry.getName().contains(current_path) && entry.getName().substring(entry.getName().indexOf(current_path) + current_path.length()).equals(name)) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(tarArchiveInputStream));
                        String line;
                        int linesCount = 0;
                        String[] lastLines = new String[k];
                        while ((line = br.readLine()) != null) {
                            lastLines[linesCount % k] = line;
                            linesCount++;
                        }
                        int start = linesCount > k ? linesCount % k : 0;
                        for (int i = 0; i < k; i++) {
                            if (lastLines[(start + i) % k] != null)
                                out.add(lastLines[(start + i) % k]);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (out.isEmpty()) out.add("No such file.");
        return out;
    }

    public static void tail_command(String text, DefaultListModel<String> listModel) {
        if (text.length() <= 7) // > tail
            return;
        String sub_path = text.substring(text.indexOf("tail") + 5);
        if (files.contains(sub_path)) {
            ArrayList<String> file_strings = new ArrayList<>();
            file_strings = read_file(sub_path, 10);

            // добавляем на экран строки из файла
            for (int i = 0; i < file_strings.size(); i++) {
                listModel.add(listModel.getSize(), file_strings.get(i));
            }
        }
        else listModel.add(listModel.getSize(), "No such file.");
    }

    public static void checkCommand(String text, DefaultListModel<String> listModel) {
        // надо как-то получить начало text
        String text_start = "";
        int k = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ' ') {
                k++;
                if (k == 2) break;
            }
            text_start = text_start + text.charAt(i);
        }
        switch (text_start) {
            case "> ls":
                ArrayList<String> arrayList = unTar("src/test_arh.tar");
                for (int i = 0; i < arrayList.size(); i++)
                    listModel.add(listModel.getSize(), arrayList.get(i));
                break;

            case "> cd":
                if (text.length() <= 5) // > cd
                    break;

                unTar("src/test_arh.tar"); // чтобы в directories были актуальные папки
                if (text.length() == 6 && text.charAt(text.length()-1) == '/') { // cd /
                    current_path = start_path;
                    break;
                }

                if (text.endsWith("..")) { // cd ..
                    int count = 0;
                    int j = 0;
                    for (j = current_path.length()-1; j >= 0; j--) {
                        if (current_path.charAt(j) == '/') {
                            count++;
                            if (count == 2) break;
                        }
                    }
                    if (count == 2) {
                        current_path = current_path.substring(0, j + 1);
                        break;
                    } // else ?
                }

                String sub_path = text.substring(text.indexOf("cd") + 3); // cd dir_1
                if (directories.contains(sub_path + "/")) {
                    current_path = current_path + sub_path + "/";
                    //System.out.println(current_path);
                }
                else listModel.add(listModel.getSize(), "No such directory.");
                break;
            case "> tail":
                unTar("src/test_arh.tar"); // чтобы в files были актуальные файлы
                tail_command(text, listModel);
                break;
            case "> pwd":
                listModel.add(listModel.getSize(), current_path);
                break;
            default:
                listModel.add(listModel.getSize(), "No such command.");
        }
    }

    public static void buttonsListeners(JTextField textField, DefaultListModel<String> listModel, JFrame window,
                                        JList<String> commands, JButton button) {
        textField.addActionListener(new ActionListener() { // обработка нажатия enter
            @Override
            public void actionPerformed(ActionEvent e) {
                listModel.add(listModel.getSize(), textField.getText());
                checkCommand(textField.getText(), listModel);
                if (textField.getText().equals("> exit")) {
                    window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
                }
                commands.ensureIndexIsVisible(listModel.getSize() - 1);
                textField.setText("> ");
            }
        });

        button.addActionListener(new ActionListener() { // обработка нажатия кнопки
            @Override
            public void actionPerformed(ActionEvent e) {
                listModel.add(listModel.getSize(), textField.getText());
                checkCommand(textField.getText(), listModel);
                if (textField.getText().equals("> exit")) {
                    window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
                }
                commands.ensureIndexIsVisible(listModel.getSize() - 1);
                textField.setText("> ");
            }
        });
    }
    public static void runCmd(Boolean script) {
        ArrayList<String> script_commands = new ArrayList<>();
        if (script) {
            script_commands = read_script(script_name);
        }
        ArrayList<String> finalScript_commands = script_commands;

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame window = new JFrame("CmdEmulator");
                JButton button = new JButton("Enter");
                button.setBounds(650,350,90,30);
                JTextField textField = new JTextField();
                textField.setText("> ");
                textField.setBounds(50,350,540,30);

                // создание панели, на которой будут крепиться все элементы
                JPanel panel = new JPanel();
                panel.setLayout(null);

                // создание списка для отображения того, что пишется в cmd
                DefaultListModel<String> listModel = new DefaultListModel<String>();
                JList<String> commands = new JList<String>(listModel);
                commands.setLayoutOrientation(JList.VERTICAL);

                JScrollPane scrollPane = new JScrollPane(); // добавляем возможность прокручивать список
                commands.setBounds(50, 50, 540, 280);
                scrollPane.setViewportView(commands);
                scrollPane.setSize(new Dimension(540, 280));

                if (script) {
                    for (String finalScriptCommand : finalScript_commands) {
                        listModel.add(listModel.getSize(), finalScriptCommand);
                        checkCommand(finalScriptCommand, listModel);
                    }
                }
                buttonsListeners(textField, listModel, window, commands, button);

                // добавляем компоненты на окно
                panel.add(button);
                panel.add(textField);
                panel.add(scrollPane);

                SpringLayout layout = new SpringLayout(); // задаём layout, чтобы все элементы отображались, как нам надо
                layout.putConstraint(SpringLayout.NORTH, button, 10, SpringLayout.SOUTH, scrollPane);
                layout.putConstraint(SpringLayout.WEST, button, 10, SpringLayout.WEST, panel);

                layout.putConstraint(SpringLayout.NORTH, textField, 10, SpringLayout.SOUTH, scrollPane);
                layout.putConstraint(SpringLayout.WEST, textField, 10, SpringLayout.EAST, button);
                layout.putConstraint(SpringLayout.EAST, textField, -10, SpringLayout.EAST, panel);

                layout.putConstraint(SpringLayout.NORTH, scrollPane, 10, SpringLayout.NORTH, panel);
                layout.putConstraint(SpringLayout.WEST, scrollPane, 10, SpringLayout.WEST, panel);
                layout.putConstraint(SpringLayout.SOUTH, scrollPane, -80, SpringLayout.SOUTH, panel);
                layout.putConstraint(SpringLayout.EAST, scrollPane, -10, SpringLayout.EAST, panel);

                panel.setLayout(layout);

                window.add(panel);

                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Dimension dimension = toolkit.getScreenSize();
                window.setLocationRelativeTo(null);
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                window.setBounds(dimension.width/2 - 400, dimension.height/2 - 250, 800, 500); // устанавливаем окно посередине экрана
                window.setVisible(true);
            }
        });
    }

    public static void main(String args[]) {
        if (args.length == 2 && args[1] != null) {
            script_name = args[1];
        }
        unTar("src/test_arh.tar");
        //
        script_name = "src/test_script.txt";
        runCmd(!script_name.isEmpty());
    }
}