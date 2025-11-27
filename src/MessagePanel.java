import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;

public class MessagePanel extends JPanel {
    private JList<EmailMessage> messageList;
    private DefaultListModel<EmailMessage> listModel;
    private JTextArea messageContentArea;

    public MessagePanel() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Панель списка сообщений
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Входящие сообщения",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12))
        );

        listModel = new DefaultListModel<>();
        messageList = new JList<>(listModel);
        messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        messageList.setCellRenderer(new MessageListRenderer());

        JScrollPane listScrollPane = new JScrollPane(messageList);
        listScrollPane.setPreferredSize(new Dimension(400, 200));
        listPanel.add(listScrollPane, BorderLayout.CENTER);

        // Панель содержимого сообщения
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Содержимое сообщения",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12))
        );

        messageContentArea = new JTextArea();
        messageContentArea.setEditable(false);
        messageContentArea.setLineWrap(true);
        messageContentArea.setWrapStyleWord(true);
        messageContentArea.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JScrollPane contentScrollPane = new JScrollPane(messageContentArea);
        contentScrollPane.setPreferredSize(new Dimension(400, 200));
        contentPanel.add(contentScrollPane, BorderLayout.CENTER);

        // Разделение панелей
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listPanel, contentPanel);
        splitPane.setResizeWeight(0.5);

        add(splitPane, BorderLayout.CENTER);

        setupListeners();
    }

    private void setupListeners() {
        messageList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                EmailMessage selectedMessage = messageList.getSelectedValue();
                if (selectedMessage != null) {
                    displayMessage(selectedMessage);
                }
            }
        });
    }

    public void updateMessages(List<EmailMessage> messages) {
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            if (messages != null) {
                for (EmailMessage message : messages) {
                    listModel.addElement(message);
                }
            }
        });
    }

    private void displayMessage(EmailMessage message) {
        StringBuilder content = new StringBuilder();
        content.append("От: ").append(message.getFrom()).append("\n");
        content.append("Тема: ").append(message.getSubject()).append("\n");
        content.append("Дата: ").append(message.getDate()).append("\n");
        content.append("\n").append(message.getBody());

        messageContentArea.setText(content.toString());
        messageContentArea.setCaretPosition(0);
    }

    // Кастомный рендерер для списка сообщений
    private static class MessageListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof EmailMessage) {
                EmailMessage message = (EmailMessage) value;
                String displayText = String.format("<html><b>%s</b><br/><small>От: %s</small></html>",
                        message.getSubject(), message.getFrom());
                setText(displayText);
            }

            return this;
        }
    }
}