import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.*;

public class AIAnalysis  extends JFrame {

    // ================= COLORS =================
    private static final Color BG_DARK = new Color(18, 18, 18);
    private static final Color CARD_BG = new Color(28, 28, 28);
    private static final Color TEXT_WHITE = Color.WHITE;
    private static final Color TEXT_SECONDARY = new Color(180, 180, 180);

    private static final Color BTN_PRIMARY = new Color(76, 110, 245);
    private static final Color BTN_SUCCESS = new Color(46, 204, 113);
    private static final Color BTN_DANGER = new Color(231, 76, 60);

    // ================= UI =================
    private JTextArea inputArea, explanationArea, humanizedArea;
    private JLabel statusLabel, badgeLabel;
    private JProgressBar progressBar;
    private JButton analyzeBtn, humanizeBtn, clearBtn;

    private DefaultListModel<HistoryItem> historyModel;
    private JList<HistoryItem> historyList;

    // ================= API =================
    private static final String API_KEY = "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";
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    // ================= CONSTRUCTOR =================
    public AIAnalysis() {
        setTitle("AI Analysis");
        setSize(1400, 780);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainLayout(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        getContentPane().setBackground(BG_DARK);
        setVisible(true);
    }

    // ================= HEADER =================
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);
        header.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("AI Analysis");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(TEXT_WHITE);

        JLabel subtitle = new JLabel("AI Text Detection, Humanization & History");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(TEXT_SECONDARY);

        JPanel box = new JPanel(new GridLayout(2, 1));
        box.setBackground(BG_DARK);
        box.add(title);
        box.add(subtitle);

        header.add(box, BorderLayout.WEST);
        return header;
    }

    // ================= MAIN =================
    private JPanel buildMainLayout() {
        JPanel main = new JPanel(new BorderLayout(20, 0));
        main.setBorder(new EmptyBorder(20, 25, 20, 25));
        main.setBackground(BG_DARK);

        main.add(buildHistoryPanel(), BorderLayout.WEST);
        main.add(buildContentPanel(), BorderLayout.CENTER);

        return main;
    }

    // ================= HISTORY =================
    private JPanel buildHistoryPanel() {
        historyModel = new DefaultListModel<>();
        historyList = new JList<>(historyModel);
        historyList.setBackground(new Color(35, 35, 35));
        historyList.setForeground(TEXT_WHITE);
        historyList.setBorder(new EmptyBorder(8, 8, 8, 8));

        historyList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                HistoryItem item = historyList.getSelectedValue();
                if (item != null) {
                    inputArea.setText(item.input);
                    explanationArea.setText(item.explanation);
                    humanizedArea.setText(item.humanized);
                    badgeLabel.setText("Loaded from History");
                }
            }
        });

        return createCard("History", new JScrollPane(historyList), 260);
    }

    // ================= CONTENT =================
    private JPanel buildContentPanel() {
        JPanel content = new JPanel(new GridLayout(1, 2, 20, 0));
        content.setBackground(BG_DARK);

        content.add(buildInputCard());
        content.add(buildOutputCard());
        return content;
    }

    private JPanel buildInputCard() {
        inputArea = createTextArea(true);
        return createCard("Input Text", new JScrollPane(inputArea), -1);
    }

    private JPanel buildOutputCard() {
        badgeLabel = new JLabel("Status Waiting");
        badgeLabel.setForeground(TEXT_WHITE);

        explanationArea = createTextArea(false);
        humanizedArea = createTextArea(false);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(CARD_BG);

        box.add(badgeLabel);
        box.add(Box.createVerticalStrut(10));
        box.add(createCard("Explanation", new JScrollPane(explanationArea), -1));
        box.add(Box.createVerticalStrut(10));
        box.add(createCard("Humanized Output", new JScrollPane(humanizedArea), -1));

        return box;
    }

    // ================= FOOTER =================
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG_DARK);
        footer.setBorder(new EmptyBorder(15, 25, 15, 25));

        analyzeBtn = createButton("Analyze", BTN_PRIMARY);
        humanizeBtn = createButton("Humanize", BTN_SUCCESS);
        clearBtn = createButton("Clear", BTN_DANGER);

        analyzeBtn.addActionListener(e -> analyzeText());
        humanizeBtn.addActionListener(e -> humanizeText());
        clearBtn.addActionListener(e -> clearAll());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.setBackground(BG_DARK);
        buttons.add(analyzeBtn);
        buttons.add(humanizeBtn);
        buttons.add(clearBtn);

        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(TEXT_SECONDARY);

        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setIndeterminate(true);

        JPanel status = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        status.setBackground(BG_DARK);
        status.add(statusLabel);
        status.add(progressBar);

        footer.add(buttons, BorderLayout.WEST);
        footer.add(status, BorderLayout.EAST);
        return footer;
    }

    // ================= HELPERS =================
    private JPanel createCard(String title, JComponent content, int width) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel label = new JLabel(title);
        label.setForeground(TEXT_WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));

        card.add(label, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        if (width > 0) card.setPreferredSize(new Dimension(width, 0));
        return card;
    }

    private JTextArea createTextArea(boolean editable) {
        JTextArea area = new JTextArea();
        area.setEditable(editable);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setForeground(TEXT_WHITE);
        area.setCaretColor(TEXT_WHITE);
        area.setBackground(new Color(40, 40, 40));
        area.setBorder(new EmptyBorder(10, 10, 10, 10));
        return area;
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(new EmptyBorder(10, 22, 10, 22));
        return btn;
    }

    // ================= LOGIC =================
    private void analyzeText() {
        runAI("Analyze the text and determine if it is AI written or human written");
    }

    private void humanizeText() {
        runAI("Rewrite the text in natural human language with the same meaning");
    }

    private void runAI(String instruction) {
        String text = inputArea.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter text first");
            return;
        }

        progressBar.setVisible(true);
        statusLabel.setText("Processing");
        analyzeBtn.setEnabled(false);

        new Thread(() -> {
            String response = callOpenRouter(instruction + "\n\nText:\n" + text);

            SwingUtilities.invokeLater(() -> {
                explanationArea.setText(response);
                humanizedArea.setText(response);
                badgeLabel.setText("Analysis Complete");
                statusLabel.setText("Done");
                progressBar.setVisible(false);
                analyzeBtn.setEnabled(true);
                saveHistory(text, response, response);
            });
        }).start();
    }

    // ================= API CALL =================
    private String callOpenRouter(String prompt) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject body = new JSONObject();
            body.put("model", "meta-llama/llama-3.2-3b-instruct:free");

            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "user").put("content", prompt));
            body.put("messages", messages);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);

            JSONObject json = new JSONObject(sb.toString());
            return json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private void saveHistory(String input, String explanation, String humanized) {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        historyModel.add(0, new HistoryItem(time, input, explanation, humanized));
    }

    private void clearAll() {
        inputArea.setText("");
        explanationArea.setText("");
        humanizedArea.setText("");
        badgeLabel.setText("Status Waiting");
        statusLabel.setText("Ready");
    }

    static class HistoryItem {
        String time, input, explanation, humanized;
        HistoryItem(String t, String i, String e, String h) {
            time = t; input = i; explanation = e; humanized = h;
        }
        public String toString() {
            return "[" + time + "] " + (input.length() > 30 ? input.substring(0, 30) : input);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AIAnalysis::new);
    }
}
