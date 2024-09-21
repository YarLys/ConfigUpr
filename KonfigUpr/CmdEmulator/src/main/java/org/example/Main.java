package org.example;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

public class Main{
    public static String start_path = "test_arh/";
    public static String script_name = "";
    public static String current_path = "test_arh/";
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
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }

    public static void checkCommand(String text, DefaultListModel<String> listModel) {
        switch (text) {
            case "> ls":
                ArrayList<String> arrayList = unTar("src/test_arh.tar");
                for (int i = 0; i < arrayList.size(); i++)
                    listModel.add(listModel.getSize(), arrayList.get(i));
                break;
            case "> cd":

                break;
            case "> tail":

                break;
            case "> pwd":

                break;
        }
    }
    public static void runCmd() {
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

                textField.addActionListener(new ActionListener() { // обработка нажатия enter
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        listModel.add(listModel.getSize(), textField.getText());
                        checkCommand(textField.getText(), listModel);
                        commands.ensureIndexIsVisible(listModel.getSize() - 1);
                        textField.setText("> ");
                    }
                });

                button.addActionListener(new ActionListener() { // обработка нажатия кнопки
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        listModel.add(listModel.getSize(), textField.getText());
                        checkCommand(textField.getText(), listModel);
                        commands.ensureIndexIsVisible(listModel.getSize() - 1);
                        textField.setText("> ");
                    }
                });

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
        //start_path = args[0];
        //script_name = args[1];
        runCmd();
    }
}