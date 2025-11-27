import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

public class EmailPanel extends JPanel {
    private EmailService emailService;
    private JLabel emailLabel;
    private JLabel statusLabel;
    private JButton createEmailButton;
    private JButton refreshButton;
    private JButton copyEmailButton;

    public EmailPanel(EmailService emailService) {
        this.emailService = emailService;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(240, 240, 240));

        // Получаем шрифты из MainFrame
        Font mainFont = MainFrame.getMainFont();
        Font boldFont = MainFrame.getBoldFont();
        Font smallFont = MainFrame.getSmallFont();

        // Панель с кнопками
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        createEmailButton = new JButton("Создать временный email");
        refreshButton = new JButton("Обновить сообщения");
        copyEmailButton = new JButton("Копировать email");

        // Устанавливаем одинаковый размер для всех кнопок
        Dimension buttonSize = new Dimension(200, 45);
        createEmailButton.setPreferredSize(buttonSize);
        refreshButton.setPreferredSize(buttonSize);
        copyEmailButton.setPreferredSize(buttonSize);

        // Стилизация кнопок
        stylePrimaryButton(createEmailButton);
        stylePrimaryButton(refreshButton);
        styleSecondaryButton(copyEmailButton);

        copyEmailButton.setEnabled(false);

        buttonPanel.add(createEmailButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(copyEmailButton);

        // Панель с email
        JPanel emailPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        emailPanel.setBackground(new Color(240, 240, 240));
        emailLabel = new JLabel("Email не создан");
        emailLabel.setFont(boldFont.deriveFont(14f));
        emailLabel.setForeground(new Color(0, 100, 200));

        JLabel currentEmailLabel = new JLabel("Текущий email: ");
        currentEmailLabel.setFont(mainFont);

        emailPanel.add(currentEmailLabel);
        emailPanel.add(emailLabel);

        // Панель статуса
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(240, 240, 240));
        JLabel statusTextLabel = new JLabel("Статус: ");
        statusTextLabel.setFont(mainFont);

        statusLabel = new JLabel("Готов к работе");
        statusLabel.setFont(boldFont);
        statusLabel.setForeground(new Color(24, 129, 20));

        statusPanel.add(statusTextLabel);
        statusPanel.add(statusLabel);

        // Основная компоновка
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(new Color(240, 240, 240));
        northPanel.add(buttonPanel, BorderLayout.NORTH);
        northPanel.add(emailPanel, BorderLayout.CENTER);
        northPanel.add(statusPanel, BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);

        setupListeners();
    }

    private void stylePrimaryButton(JButton button) {
        Font buttonFont = MainFrame.getBoldFont();

        button.setBackground(new Color(0, 100, 200));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                ButtonModel model = b.getModel();

                // Рисуем фон
                if (model.isPressed()) {
                    g.setColor(new Color(0, 60, 160));
                } else if (model.isRollover()) {
                    g.setColor(new Color(0, 80, 180));
                } else {
                    g.setColor(new Color(0, 100, 200));
                }
                g.fillRect(0, 0, c.getWidth(), c.getHeight());

                // Рисуем текст
                g.setColor(Color.WHITE);
                g.setFont(buttonFont);
                FontMetrics fm = g.getFontMetrics();
                String text = b.getText();
                int x = (c.getWidth() - fm.stringWidth(text)) / 2;
                int y = (c.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString(text, x, y);
            }
        });

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 80, 180));
                button.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 100, 200));
                button.repaint();
            }

            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 60, 160));
                button.repaint();
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 80, 180));
                button.repaint();
            }
        });
    }

    private void styleSecondaryButton(JButton button) {
        Font buttonFont = MainFrame.getBoldFont();

        button.setBackground(new Color(120, 120, 120));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                ButtonModel model = b.getModel();

                // Рисуем фон
                if (model.isPressed()) {
                    g.setColor(new Color(80, 80, 80));
                } else if (model.isRollover()) {
                    g.setColor(new Color(100, 100, 100));
                } else {
                    g.setColor(new Color(120, 120, 120));
                }
                g.fillRect(0, 0, c.getWidth(), c.getHeight());

                // Рисуем текст
                g.setColor(Color.WHITE);
                g.setFont(buttonFont);
                FontMetrics fm = g.getFontMetrics();
                String text = b.getText();
                int x = (c.getWidth() - fm.stringWidth(text)) / 2;
                int y = (c.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g.drawString(text, x, y);
            }
        });

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 100, 100));
                button.repaint();
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(120, 120, 120));
                button.repaint();
            }

            public void mousePressed(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(80, 80, 80));
                button.repaint();
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 100, 100));
                button.repaint();
            }
        });
    }

    private void setupListeners() {
        createEmailButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createEmailButton.setEnabled(false);
                refreshButton.setEnabled(false);
                copyEmailButton.setEnabled(false);
                statusLabel.setText("Создание email...");
                statusLabel.setForeground(Color.BLUE);

                new Thread(() -> {
                    try {
                        EmailAccount account = emailService.createNewEmail();
                        SwingUtilities.invokeLater(() -> {
                            if (account != null) {
                                String email = account.getEmail();
                                emailLabel.setText(email);
                                copyEmailButton.setEnabled(true);

                                JOptionPane.showMessageDialog(EmailPanel.this,
                                        "Новый email создан: " + email,
                                        "Успех",
                                        JOptionPane.INFORMATION_MESSAGE);
                                statusLabel.setText("Email создан");
                                statusLabel.setForeground(new Color(24, 129, 20));
                            } else {
                                JOptionPane.showMessageDialog(EmailPanel.this,
                                        "Не удалось создать email. Сервисы временной почты недоступны.",
                                        "Ошибка",
                                        JOptionPane.ERROR_MESSAGE);
                                statusLabel.setText("Ошибка создания");
                                statusLabel.setForeground(Color.RED);
                            }
                            createEmailButton.setEnabled(true);
                            refreshButton.setEnabled(true);
                        });
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(EmailPanel.this,
                                    "Ошибка при создании email: " + ex.getMessage(),
                                    "Ошибка",
                                    JOptionPane.ERROR_MESSAGE);
                            statusLabel.setText("Ошибка");
                            statusLabel.setForeground(Color.RED);
                            createEmailButton.setEnabled(true);
                            refreshButton.setEnabled(true);
                        });
                    }
                }).start();
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (emailService.getCurrentAccount() != null) {
                    refreshButton.setEnabled(false);
                    statusLabel.setText("Обновление сообщений...");
                    statusLabel.setForeground(Color.BLUE);

                    new Thread(() -> {
                        try {
                            emailService.refreshMessages();
                            SwingUtilities.invokeLater(() -> {
                                statusLabel.setText("Сообщения обновлены");
                                statusLabel.setForeground(new Color(24, 129, 20));
                                refreshButton.setEnabled(true);
                            });
                        } catch (Exception ex) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(EmailPanel.this,
                                        "Ошибка при обновлении: " + ex.getMessage(),
                                        "Ошибка",
                                        JOptionPane.ERROR_MESSAGE);
                                statusLabel.setText("Ошибка обновления");
                                statusLabel.setForeground(Color.RED);
                                refreshButton.setEnabled(true);
                            });
                        }
                    }).start();
                } else {
                    JOptionPane.showMessageDialog(EmailPanel.this,
                            "Сначала создайте email",
                            "Внимание",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        copyEmailButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (emailService.getCurrentAccount() != null) {
                    String email = emailService.getCurrentAccount().getEmail();
                    copyToClipboard(email);

                    String originalText = copyEmailButton.getText();

                    copyEmailButton.setText("Скопировано!");
                    copyEmailButton.setBackground(new Color(24, 129, 20));
                    copyEmailButton.repaint();

                    Timer timer = new Timer(2000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            copyEmailButton.setText(originalText);
                            copyEmailButton.setBackground(new Color(120, 120, 120));
                            copyEmailButton.repaint();
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();

                    statusLabel.setText("Email скопирован в буфер");
                    statusLabel.setForeground(Color.BLUE);
                }
            }
        });
    }

    private void copyToClipboard(String text) {
        try {
            StringSelection stringSelection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);

            JOptionPane.showMessageDialog(this,
                    "Email скопирован в буфер обмена:\n" + text,
                    "Успех",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка при копировании: " + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateEmailLabel(String email) {
        emailLabel.setText(email);
        copyEmailButton.setEnabled(true);
    }

    public void setStatus(String status, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
            statusLabel.setForeground(color);
        });
    }
}