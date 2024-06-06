package org.yuri.excel;

import com.aspose.cells.*;
import org.yuri.model.Line;

import java.util.HashMap;

public class ExcelDataReader {

    private int branch;

    public HashMap<Integer, HashMap<Integer, Line>> execute(String filePath) {
        HashMap<Integer, HashMap<Integer, Line>> lines = new HashMap<>();
        parseBranchFromFilePath(filePath);

        try {
            Workbook workbook = new Workbook(filePath);
            HashMap<Integer, Line> combinedLines = new HashMap<>();

            for (int i = 0; i < 2; i++) {
                combinedLines.putAll(processWorksheet(workbook.getWorksheets().get(i)));
            }

            lines.put(branch, combinedLines);

        } catch (Exception e) {
            throw new RuntimeException("Error reading Excel file", e);
        }

        return lines;
    }

    private void parseBranchFromFilePath(String filePath) {
        String[] fileSplit = filePath.split("_");
        branch = Integer.parseInt(fileSplit[1]);
    }

    private HashMap<Integer, Line> processWorksheet(Worksheet worksheet) {
        HashMap<Integer, Line> lines = new HashMap<>();
        Cells cells = worksheet.getCells();
        int maxDataRow = cells.getMaxDataRow() + 1;

        for (int i = 12; i < maxDataRow; i++) {
            Row row = cells.checkRow(i);
            if (row == null) continue;

            Cell cfopCell = row.getCellOrNull(2);
            if (cfopCell == null || !cfopCell.isNumericValue()) continue;

            Line line = createLineFromRow(row);
            if (line != null) {
                int cfop = row.getCellOrNull(2).getIntValue();
                lines.put(cfop, line);
            }
        }

        return lines;
    }

    private Line createLineFromRow(Row row) {
        try {
            String description = row.getCellOrNull(4).getStringValue();
            int cfop = row.getCellOrNull(2).getIntValue();
            double total = row.getCellOrNull(5).getDoubleValue();
            double base = row.getCellOrNull(7).getDoubleValue();
            double icms = row.getCellOrNull(8).getDoubleValue();
            return new Line(description, cfop, total, base, icms);

        } catch (Exception e) {
            return null;
        }
    }
}
