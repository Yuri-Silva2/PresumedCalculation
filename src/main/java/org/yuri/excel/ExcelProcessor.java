package org.yuri.excel;

import com.aspose.cells.*;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import org.yuri.model.Line;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExcelProcessor implements Runnable {

    private final ArrayList<Line> remainingLines = new ArrayList<>();

    private Workbook workbook;

    private final ProgressBar progressBar;
    private final Button generateButton;
    private final Button saveButton;
    private final Button selectFilesButton;
    private final Button exampleFileButton;

    private final ExcelQueue queue;

    public ExcelProcessor(Button generateButton, Button saveButton, ProgressBar progressBar, Button selectFilesButton,
                          Button exampleFileButton) {
        this.queue = new ExcelQueue();
        this.generateButton = generateButton;
        this.saveButton = saveButton;
        this.progressBar = progressBar;
        this.selectFilesButton = selectFilesButton;
        this.exampleFileButton = exampleFileButton;
    }

    public void addToQueue(Object o) {
        queue.enqueue(((File) o).getPath());
    }

    private void process() {
        loadWorkbook();

        while (!queue.isEmpty()) {
            String filePath = queue.dequeue();

            if (filePath != null) {
                HashMap<Integer, HashMap<Integer, Line>> lines = new ExcelDataReader().execute(filePath);
                updateWorksheet(lines);
            }
        }
    }

    private void updateWorksheet(HashMap<Integer, HashMap<Integer, Line>> lines) {
        Worksheet sheet = workbook.getWorksheets().get(0);
        Cells cells = sheet.getCells();
        int maxDataRow = cells.getMaxDataRow();

        String cnpj = "";
        HashMap<Integer, Line> branchLines = null;

        for (int i = 1; i <= maxDataRow; i++) {
            Cell cell = cells.get(i, 0);
            if (cell == null) continue;

            String currentCNPJ = cell.getStringValue();

            if (!cnpj.equals(currentCNPJ)) {
                if (branchLines != null && !branchLines.isEmpty()) {

                    for (Map.Entry<Integer, Line> entry : branchLines.entrySet()) {
                        entry.getValue().setCnpj(cnpj);
                        remainingLines.add(entry.getValue());
                    }
                }

                cnpj = currentCNPJ;
                int branch = getBranchFromCNPJ(cnpj);
                if (branch == 0) continue;
                branchLines = lines.get(branch);
            }

            if (branchLines == null) continue;

            int cfop = cells.get(i, 3).getIntValue();

            Line line = branchLines.get(cfop);

            if (line != null) {
                cells.get(i, 6).setValue(line.total());
                cells.get(i, 7).setValue(line.base());
                cells.get(i, 8).setValue(line.icms());

                branchLines.remove(cfop);
            }
        }

        int currentRow = maxDataRow + 1;
        for (Line entry : remainingLines) {
            cells.get(currentRow, 0).setValue(entry.getCnpj());
            cells.get(currentRow, 3).setValue(entry.cfop());
            cells.get(currentRow, 4).setValue(entry.description());
            cells.get(currentRow, 6).setValue(entry.total());
            cells.get(currentRow, 7).setValue(entry.base());
            cells.get(currentRow, 8).setValue(entry.icms());

            setCellColor(cells.get(currentRow, 0), Color.getRed());
            setCellColor(cells.get(currentRow, 3), Color.getRed());
            setCellColor(cells.get(currentRow, 4), Color.getRed());
            setCellColor(cells.get(currentRow, 6), Color.getRed());
            setCellColor(cells.get(currentRow, 7), Color.getRed());
            setCellColor(cells.get(currentRow, 8), Color.getRed());
            currentRow++;
        }
    }

    private void setCellColor(Cell cell, Color color) {
        Style style = cell.getStyle();
        style.setForegroundColor(color);
        style.setPattern(BackgroundType.SOLID);
        cell.setStyle(style);
    }

    private int getBranchFromCNPJ(String cnpj) {
        return switch (cnpj) {
            case "86900925/0001-04" -> 1000;
            case "86900925/0003-76" -> 1102;
            case "86900925/0004-57" -> 1003;
            case "86900925/0005-38" -> 1004;
            case "86900925/0006-19" -> 1005;
            case "86900925/0008-80" -> 1107;
            case "86900925/0010-03" -> 1209;
            case "86900925/0011-86" -> 1010;
            case "86900925/0012-67" -> 1011;
            case "86900925/0013-48" -> 1112;
            default -> 0;
        };
    }

    private void loadWorkbook() {
        String filePath = System.getProperty("user.home") + "\\Documents\\PresumedCalculation\\example.xlsx";
        try (InputStream inputStream = new FileInputStream(filePath)) {
            workbook = new Workbook(inputStream);

        } catch (IOException e) {
            throw new RuntimeException("Error loading workbook", e);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing Aspose Cells Workbook", e);
        }
    }

    public void save(String filePath) {
        try {
            workbook.save(filePath, SaveFormat.XLSX);

            exampleFileButton.setDisable(false);
            selectFilesButton.setDisable(false);
            generateButton.setVisible(true);
            saveButton.setVisible(false);

        } catch (IOException e) {
            throw new RuntimeException("Error saving workbook", e);
        } catch (Exception e) {
            throw new RuntimeException("Error saving Aspose Cells Workbook", e);
        }
    }

    @Override
    public void run() {
        try {
            process();
            Thread.sleep(5000);
            updateUIOnCompletion();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void updateUIOnCompletion() {
        generateButton.setVisible(false);
        saveButton.setVisible(true);
        progressBar.setProgress(1.0);
    }
}
