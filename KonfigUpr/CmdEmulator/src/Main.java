import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main{
    private static void runCmd() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame window = new JFrame("CmdEmulator");
                JButton button = new JButton("Enter");
                button.setBounds(650,350,90,30);
                JTextField textField = new JTextField();
                textField.setText("> ");
                textField.setBounds(50,350,540,30);

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
                        commands.ensureIndexIsVisible(listModel.getSize() - 1);
                        textField.setText("> ");
                    }
                });

                button.addActionListener(new ActionListener() { // обработка нажатия кнопки
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        listModel.add(listModel.getSize(), textField.getText());
                        commands.ensureIndexIsVisible(listModel.getSize() - 1);
                        textField.setText("> ");
                    }
                });

                // добавляем компоненты на окно
                panel.add(button);
                panel.add(textField);
                panel.add(scrollPane);

                SpringLayout layout = new SpringLayout();
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
        runCmd();
    }
}