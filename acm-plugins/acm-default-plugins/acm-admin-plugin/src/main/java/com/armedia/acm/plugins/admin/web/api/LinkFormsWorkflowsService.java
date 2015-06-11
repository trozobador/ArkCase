package com.armedia.acm.plugins.admin.web.api;


import com.armedia.acm.activiti.model.AcmProcessDefinition;
import com.armedia.acm.activiti.services.AcmBpmnService;
import com.armedia.acm.plugins.admin.exception.AcmLinkFormsWorkflowException;
import com.armedia.acm.plugins.ecm.service.AcmFileTypesService;
import com.google.gson.JsonObject;
import org.activiti.engine.impl.util.json.JSONArray;
import org.apache.commons.codec.binary.Hex;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.SheetUtil;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.codehaus.plexus.util.FileUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

/**
 * Created by admin on 6/12/15.
 */
public class LinkFormsWorkflowsService {
    private Logger log = LoggerFactory.getLogger(LdapConfigurationService.class);

    private String configurationLocation;
    private String configurationFile;
    private String configurationFileBackupTemplate;
    private String configurationFileBackupRegex;

    private final String PROP_VALUE  = "value";
    private final String PROP_TYPE  = "type";
    private final String PROP_COLOR  = "color";
    private final String PROP_BG_COLOR  = "bgColor";
    private final String PROP_FONT_SIZE  = "fontSize";
    private final String PROP_READONLY  = "readonly";

    private final String PROP_CELLS  = "cells";
    private final String PROP_COLUMNS_WIDTH  = "columnsWidths";
    private final String PROP_META = "meta";
    private final String DEFAULT_BG_COLOR = "#FFFFFF";

    private final String COL_TYPE_RULE_NAME = "ruleName";
    private final String COL_TYPE_FILE_TYPE = "fileType";
    private final String COL_TYPE_START_PROCESS = "startProcess";
    private final String COL_TYPE_PROCESS_NAME = "processName";
    private final String COL_TYPE_PRIORITY = "priority";
    private final String COL_TYPE_DUE_DATE = "dueDate";


    private final String[] START_PROCESS_VALUES = new String[] {"", "true", "false"};

    private AcmBpmnService acmBpmnService;
    private AcmFileTypesService acmFileTypesService;

    /**
     * Return Excel workflow configuration as JSON
     * @return
     * @throws AcmLinkFormsWorkflowException
     */
    public JSONObject retrieveConfigurationAsJson() throws AcmLinkFormsWorkflowException {
        try {
            FileInputStream file = null;
            try {
                file = new FileInputStream(new File(configurationLocation + configurationFile));
                XSSFWorkbook workbook = new XSSFWorkbook(file);

                // Get Sheet number 0;
                XSSFSheet sheet = workbook.getSheetAt(0);

                // Find longest row
                int lastCell = 0;
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    if (sheet.getRow(i) != null) {
                        if (sheet.getRow(i).getLastCellNum() > lastCell) {
                            lastCell = sheet.getRow(i).getLastCellNum();
                        }
                    }
                }
                // Get columns widths
                List<Double> columnsWidths = new ArrayList();
                for (int i = 0; i < lastCell; i++) {
                    columnsWidths.add(SheetUtil.getColumnWidth(sheet, i, false, 0, sheet.getLastRowNum()));
                }

                List<List<Map<String, Object>>> cellsMatrix = new ArrayList();
                Map <Integer, String> columnsTypes = new HashMap();

                // Process rows and cells
                for (int rowNum = 0; rowNum < sheet.getLastRowNum(); rowNum++) {
                    List<Map<String, Object>> cellsRow = new ArrayList();
                    XSSFRow row = (XSSFRow)sheet.getRow(rowNum);
                    if (row != null) {
                        for (int colNum = 0; colNum < row.getLastCellNum(); colNum++) {

                            XSSFCell cell = (XSSFCell) row.getCell(colNum);

                            // Get type if available
                            String type = "";
                            if (columnsTypes.containsKey(colNum)) {
                                type = columnsTypes.get(colNum);
                            }

                            if (cell.getCellComment() != null) {
                                String comment = cell.getCellComment().getString().getString();
                                switch (comment) {
                                    case COL_TYPE_RULE_NAME:
                                        columnsTypes.put(colNum, COL_TYPE_RULE_NAME);
                                        break;

                                    case COL_TYPE_FILE_TYPE:
                                        columnsTypes.put(colNum, COL_TYPE_FILE_TYPE);
                                        break;

                                    case COL_TYPE_START_PROCESS:
                                        columnsTypes.put(colNum, COL_TYPE_START_PROCESS);
                                        break;

                                    case COL_TYPE_PROCESS_NAME:
                                        columnsTypes.put(colNum, COL_TYPE_PROCESS_NAME);
                                        break;

                                    case COL_TYPE_PRIORITY:
                                        columnsTypes.put(colNum, COL_TYPE_PRIORITY);
                                        break;

                                    case COL_TYPE_DUE_DATE:
                                        columnsTypes.put(colNum, COL_TYPE_DUE_DATE);
                                        break;
                                }
                            }

                            Object value = "";
                            if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING) {
                                value = cell.getStringCellValue();
                            } else if (cell.getCellType() == XSSFCell.CELL_TYPE_BOOLEAN) {
                                value = String.valueOf(cell.getBooleanCellValue());
                            } else if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
                                value = cell.getNumericCellValue();
                            }

                            XSSFCellStyle cellStyle = cell.getCellStyle();
                            XSSFFont cellFont = cellStyle.getFont();

                            String color = "#" + Hex.encodeHexString(cellFont.getXSSFColor().getRgb());
                            Boolean isLocked = false;
                            String bgColor = "";
                            if (cellStyle != null) {
                                if (cellStyle.getFillForegroundColorColor() != null) {
                                    bgColor = "#" + Hex.encodeHexString(((XSSFColor) cellStyle.getFillForegroundColorColor()).getRgb());
                                } else if (cellStyle.getFillBackgroundColorColor() != null) {
                                    bgColor = "#" + Hex.encodeHexString(((XSSFColor) cellStyle.getFillBackgroundColorColor()).getRgb());
                                } else {
                                    bgColor = DEFAULT_BG_COLOR;
                                }

                                isLocked = cellStyle.getLocked();
                            }
                            int fontSize = cellFont.getFontHeightInPoints();

                            Map<String, Object> cellObj = new HashMap();
                            cellObj.put(PROP_VALUE, value);
                            cellObj.put(PROP_TYPE, type);
                            cellObj.put(PROP_COLOR, color);
                            cellObj.put(PROP_BG_COLOR, bgColor);
                            cellObj.put(PROP_READONLY, isLocked);
                            cellObj.put(PROP_FONT_SIZE, fontSize);

                            cellsRow.add(cellObj);
                        }
                    }

                    // Add tail cells if required
                    if (cellsRow.size() < (lastCell) ) {
                        int tailSize = lastCell - cellsRow.size();
                        for (int i = 0; i < tailSize; i++) {
                            Map <String, Object> emptyCell = new HashMap();
                            emptyCell.put(PROP_BG_COLOR, DEFAULT_BG_COLOR);
                            emptyCell.put(PROP_READONLY, true);
                            cellsRow.add(emptyCell);
                        }
                    }

                    cellsMatrix.add(cellsRow);
                }

                // Get Process Names. There are no ways to get all processes
                List<AcmProcessDefinition> processDefinitions = acmBpmnService.listPage(0, 1000, "name", true);
                List<String> processNames = new ArrayList();
                processNames.add("");
                for (AcmProcessDefinition processDefinitionIter: processDefinitions) {
                    processNames.add(processDefinitionIter.getKey());
                }


                List<String> fileTypes= acmFileTypesService.getFileTypes();

                // Add metadata (available values or some columns)
                JSONObject metaObject = new JSONObject();
                metaObject.put(COL_TYPE_FILE_TYPE, fileTypes);
                metaObject.put(COL_TYPE_PROCESS_NAME, processNames);
                metaObject.put(COL_TYPE_START_PROCESS, START_PROCESS_VALUES);


                JSONObject configObject = new JSONObject();
                configObject.put(PROP_CELLS, cellsMatrix);
                configObject.put(PROP_COLUMNS_WIDTH, columnsWidths);
                configObject.put(PROP_META, metaObject);

                return configObject;

            } finally {
                if (file != null) {
                    file.close();
                }
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Can't retrieve Link Forms Workflows configuration file", e);
            }

            throw new AcmLinkFormsWorkflowException("Can't retrieve Link Forms Workflows configuration file", e);
        }
    }


    /**
     * Update Excel workflow configuration
     * @param newValues
     */
    public void updateConfiguration(List<List<String>> newValues) throws AcmLinkFormsWorkflowException {
        try {
            FileInputStream inputFile = null;
            FileOutputStream outputFile = null;
            try {
                inputFile = new FileInputStream(new File(configurationLocation + configurationFile));
                XSSFWorkbook workbook = new XSSFWorkbook(inputFile);

                // Get Sheet number 0;
                XSSFSheet sheet = workbook.getSheetAt(0);


                for (int rowNum = 0; rowNum < newValues.size(); rowNum++) {
                    List<String> valuesRow = newValues.get(rowNum);
                    for (int colNum = 0; colNum < valuesRow.size(); colNum++) {
                        String value = valuesRow.get(colNum);
                        if (value != null) {

                            if (sheet.getRow(rowNum) != null) {
                                XSSFCell cell = sheet.getRow(rowNum).getCell(colNum);
                                if (cell!= null && cell.getCellStyle() != null) {
                                    if (!cell.getCellStyle().getLocked()) {
                                        cell.setCellValue(value);
                                    }
                                }
                            }
                        }
                    }
                }

                inputFile.close();

                // Generate backup file name based on current time.
                String destFileName = String.format(configurationFileBackupTemplate, (new Date()).getTime());

                // Save current configuration file as backup
                FileUtils.copyFile(
                    new File(configurationLocation + configurationFile),
                    new File(configurationLocation + destFileName)
                );

                // Store updates
                outputFile = new FileOutputStream(configurationLocation + configurationFile);
                workbook.write(outputFile);
                outputFile.close();

            } finally {
                if (inputFile != null) {
                    inputFile.close();
                }

                if (outputFile != null) {
                    outputFile.close();
                }
            }


        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Can't retrieve Link Forms Workflows configuration file", e);
            }

            throw new AcmLinkFormsWorkflowException("Can't retrieve Link Forms Workflows configuration file", e);
        }
    }

    public void setConfigurationFile(String configurationFile) {
        this.configurationFile = configurationFile;
    }

    public void setConfigurationLocation(String configurationLocation) {
        this.configurationLocation = configurationLocation;
    }

    public void setConfigurationFileBackupTemplate(String configurationFileBackupTemplate) {
        this.configurationFileBackupTemplate = configurationFileBackupTemplate;
    }

    public void setConfigurationFileBackupRegex(String configurationFileBackupRegex) {
        this.configurationFileBackupRegex = configurationFileBackupRegex;
    }

    public void setAcmBpmnService(AcmBpmnService acmBpmnService) {
        this.acmBpmnService = acmBpmnService;
    }

    public void setAcmFileTypesService(AcmFileTypesService acmFileTypesService) {
        this.acmFileTypesService = acmFileTypesService;
    }
}
