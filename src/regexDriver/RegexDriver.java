package regexDriver;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import appLogger.AppLogger;



public class RegexDriver {

	private AppLogger log = AppLogger.getInstance();
	
	private AdapterForRegexDriver adapterForRegexDriver = new AdapterForRegexDriver();
	private StyledDocument doc;
	private SimpleAttributeSet attrBlack = new SimpleAttributeSet();
	private SimpleAttributeSet attrGreen = new SimpleAttributeSet();
	private SimpleAttributeSet attrRed = new SimpleAttributeSet();
	private SimpleAttributeSet attrBlue = new SimpleAttributeSet();

	private DefaultComboBoxModel<String> regexCodeModel = new DefaultComboBoxModel<String>();
	private DefaultComboBoxModel<String> sourceStringModel = new DefaultComboBoxModel<String>();

	private Pattern patternForFind;
	private Matcher matcherForFind;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RegexDriver window = new RegexDriver();
					window.frmRegexDriver.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				} // try
			}// run
		});
	}// main

	// +++++++++++++++++++++++++++++++++++++++++++++++++
	/* placeholder for trying things out */
//	private void actOnResult() {
//		// private String findComment1(String workingLine){
//		String netLine = (String) cbSourceString.getSelectedItem();
//		if (!netLine.contains(";")) {
//			postLogMessage("NO COMMENTs");
//			return;
//		} // if
//		String comment = null;
//		Pattern patternForComment = Pattern.compile("('[^']*')|(;)");
//		Matcher matcherForComment = patternForComment.matcher(netLine);
//		// int startCommentLoc = 0;
//		int startFindLoc = 0;
//		while (!matcherForComment.hitEnd()) {
//			if (matcherForComment.find(startFindLoc)) {
//				if ((matcherForComment.group(1) == null) && (matcherForComment.group(2) != null)) {
//					comment = netLine.substring(matcherForComment.end(2) - 1);
//					netLine = netLine.substring(0, matcherForComment.end(2) - 1);
//					break; // found the comment
//				} // if this is it
//				startFindLoc = Math.max(matcherForComment.end(1), matcherForComment.end(2));
//			} // if any match
//		} // while
//		postLogMessage("COMMENT: <" + comment + ">");
//		postLogMessage("netLine: <" + netLine + ">");
//	}// actOnResult
		// ---------------------------------------------------------

	private void clearResult() {
		try {
			doc.remove(0, doc.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		} // try
	}// clearResult

	private void doMatch() {
		cleanOutput();
		String logMessage0 = "No Match";
		try {
			Pattern pattern = Pattern.compile((String) cbRegexCode.getSelectedItem());
			Matcher matcher = pattern.matcher((CharSequence) cbSourceString.getSelectedItem());

			// pattern.matches(txtRegexCode.getText(), input)

			if (matcher.matches()) {
				logMessage0 = foundIt("Match");
				txtLog1.append(String.format("end = %d, start = %s%n", matcher.end(), matcher.start()));
				showGroup(matcher);
			} else {
				noChange("<< MATCHES - nothing matched >>");
			} //
		} catch (Exception e) {
			String errMessage = String.format("%s - %s%n%n", "In Catch", e.getMessage());
			txtLog1.append(errMessage);
		} //
		postLogMessage(logMessage0);
	}// doMatch

	private void doFind() {
		cleanOutput();
		String logMessage = "Not Found";
		String sourceText = (String) cbSourceString.getSelectedItem();
		btnFindNext.setEnabled(false);
		patternForFind = Pattern.compile((String) cbRegexCode.getSelectedItem());
		matcherForFind = patternForFind.matcher((CharSequence) cbSourceString.getSelectedItem());
		try {
			if (matcherForFind.find()) {
				logMessage = foundIt("Find");
				txtLog1.append(String.format("end = %d, start = %s%n", matcherForFind.end(), matcherForFind.start()));
				txtLog1.append(String.format("group = |%s|%n", matcherForFind.group()));
				txtLog1.append(String.format("Before group = |%s|%n", sourceText.substring(0, matcherForFind.start())));
				showGroup(matcherForFind);
				btnFindNext.setEnabled(true);
			} else {
				noChange("<< FIND - nothing found >>");

			} // if
		} catch (Exception e) {
			String errMessage = String.format("%s - %s%n%n", "In Catch", e.getMessage());
			txtLog1.append(errMessage);
		}
		postLogMessage(logMessage);
	}// doFind

	private void doFindNext() {
		// String logMessage;
		if (matcherForFind.hitEnd()) {
			postLogMessage("Matcher has hit end");
			noChange("<< FIND_NEXT - nothing to search >>");
		} else {
			int newStart = matcherForFind.end();
			if (matcherForFind.find(newStart)) {
				String sourceText = (String) cbSourceString.getSelectedItem();
				// String logMessage = foundIt("Find");
				txtLog1.append(String.format("end = %d, start = %s%n", matcherForFind.end(), matcherForFind.start()));
				txtLog1.append(String.format("group = |%s|%n", matcherForFind.group()));
				txtLog1.append(String.format("Before group = |%s|%n", sourceText.substring(0, matcherForFind.start())));
				showGroup(matcherForFind);
				btnFindNext.setEnabled(true);
			} else {
				noChange("<< FIND - nothing found >>");
				btnFindNext.setEnabled(false);
			} // inner if - else
		} // outer if - else
			// int newStart = matcherForFind.end();

	}// doFindNext

	private void doLookingAt() {
		cleanOutput();
		String logMessage0 = "Not looking at";

		try {
			Pattern pattern = Pattern.compile((String) cbRegexCode.getSelectedItem());
			Matcher matcher = pattern.matcher((CharSequence) cbSourceString.getSelectedItem());

			if (matcher.lookingAt()) {
				logMessage0 = foundIt("lookingAT");
				txtLog1.append(String.format("end = %d, start = %s%n", matcher.end(), matcher.start()));
				showGroup(matcher);
			} else {
				noChange("<< LOOKING AT - not looking at >>");
			} //
		} catch (Exception e) {
			String errMessage = String.format("%s - %s%n%n", "In Catch", e.getMessage());
			txtLog1.append(errMessage);
		} //
		postLogMessage(logMessage0);

	}// doLookingAt

	private void doReplace(boolean all) {
		cleanOutput();
		Pattern pattern = Pattern.compile((String) cbRegexCode.getSelectedItem());
		Matcher matcher = pattern.matcher((CharSequence) cbSourceString.getSelectedItem());
		String original = (String) cbSourceString.getSelectedItem();
		String ans;

		if (all) {
			ans = matcher.replaceAll(txtReplacement.getText());
		} else {
			ans = matcher.replaceFirst(txtReplacement.getText());

		} // if all
		SimpleAttributeSet attributeColor;
		String msg;
		if (ans.equals(original)) {
			attributeColor = attrRed;
			msg = "<< Nothing Replaced >>";
		} else {
			attributeColor = attrBlack;
			msg = ans;
		} // if
		try {
			doc.insertString(doc.getLength(), msg, attributeColor);
		} catch (BadLocationException e) {
			e.printStackTrace();
		} // try

	}// doReplace

	private void doRemoveFromList(ActionEvent actionEvent) {

		String source = actionEvent.getActionCommand();
		DefaultComboBoxModel<String> model;
		int index;
		if (source.equals(MNU_POP_REMOVE_REGEX)) {
			model = (DefaultComboBoxModel<String>) cbRegexCode.getModel();
			index = cbRegexCode.getSelectedIndex();
		} else {
			model = (DefaultComboBoxModel<String>) cbSourceString.getModel();
			index = cbSourceString.getSelectedIndex();
		} // if

		model.removeElementAt(index);
	}// doRemoveFromList

	// ......................................................
	private void saveLog() {
		String tempDir = System.getProperty("java.io.tmpdir");
		try {
			File logFile = new File(tempDir,"logFile.txt");
			FileWriter fw = new FileWriter(logFile);
			fw.write(txtLog1.getText());
			fw.close();
			btnSaveLog.setToolTipText(logFile.toString());
		} catch (IOException e) {
			System.err.printf("unable to save the Log.%n");
			e.printStackTrace();
		}
	}// saveLog

	// ---------------------------------------------------------
	private String foundIt(String typeOfOperation) {
		return typeOfOperation;
	}// foundIt

	private void noChange(String message) {
		try {
			doc.remove(0, doc.getLength());
			doc.insertString(0, message, attrRed);
		} catch (BadLocationException e) {
			e.printStackTrace();
		} // try
	}// foundItNot

	private void postLogMessage(String logMessage) {
		// String msg = String.format("%s \t%s - \t%s%n", logMessage, cbSourceString.getSelectedItem(),
		// cbRegexCode.getSelectedItem());
		String msg = String.format("%s%n", logMessage);
		txtLog1.append(msg);
	}// postLogMessage

	private void cleanOutput() {
		lblGroupBoundary.setText(" ~ - ~");
		clearResult();
	}// cleanOutput

	private void showGroup(Matcher matcher) {

		int groupCount = matcher.groupCount();
		postLogMessage(String.format("matcher.groupCount() = %d%n", matcher.groupCount()));
		for (int i = 1; i <= groupCount; i++) {
			postLogMessage(String.format("group %d = \"%s\"", i, matcher.group(i)));
		} // for

		lblGroupBoundary.setText(String.format("%d - %d", matcher.start(), matcher.end()));
		String originalString = (String) cbSourceString.getSelectedItem();
		String beforeGroup = originalString.substring(0, matcher.start());
		String group = matcher.group();
		String afterGroup = originalString.substring(matcher.end(), originalString.length());
		try {
			doc.remove(0, doc.getLength());
			doc.insertString(doc.getLength(), beforeGroup, attrBlack);
			doc.insertString(doc.getLength(), group, attrBlue);
			doc.insertString(doc.getLength(), afterGroup, attrBlack);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}//try

	}// showGroup

	private void setAttributes() {
		StyleConstants.setForeground(attrBlack, Color.BLACK);
		StyleConstants.setForeground(attrGreen, Color.GREEN);
		StyleConstants.setForeground(attrRed, Color.RED);
		StyleConstants.setForeground(attrBlue, Color.BLUE);
	}// setAttributes

	// ........................................................................


	private void setupPopupMenus() {
		JPopupMenu popupMenu1 = new JPopupMenu();
		JMenuItem removeItem1 = new JMenuItem("Remove item");
		removeItem1.setActionCommand(MNU_POP_REMOVE_REGEX);
		removeItem1.addActionListener(adapterForRegexDriver);
		popupMenu1.add(removeItem1);
		cbRegexCode.setComponentPopupMenu(popupMenu1);

		JPopupMenu popupMenu2 = new JPopupMenu();
		JMenuItem removeItem2 = new JMenuItem("Remove item");
		removeItem2.setActionCommand(MNU_POP_REMOVE_SOURCE);
		removeItem2.addActionListener(adapterForRegexDriver);
		popupMenu2.add(removeItem2);
		cbSourceString.setComponentPopupMenu(popupMenu2);
		
		JPopupMenu popupLog = new JPopupMenu();
		addPopup(txtLog1, popupLog);

		JMenuItem popupLogClear = new JMenuItem("Clear Log");
		popupLogClear.setActionCommand(PUM_LOG_CLEAR);
		popupLogClear.addActionListener(adapterForRegexDriver);
		popupLog.add(popupLogClear);

		JSeparator separator = new JSeparator();
		popupLog.add(separator);

		JMenuItem popupLogPrint = new JMenuItem("Print Log");
		popupLogPrint.setActionCommand(PUM_LOG_PRINT);
		popupLogPrint.addActionListener(adapterForRegexDriver);
		popupLog.add(popupLogPrint);
		
//		addPopup(txtLog,  popupLog);
	}// setupPopupMenus
	
	private void appClose() {
		Preferences myPrefs = Preferences.userNodeForPackage(RegexDriver.class).node(this.getClass().getSimpleName());
		Dimension dim = frmRegexDriver.getSize();
		myPrefs.putInt("Height", dim.height);
		myPrefs.putInt("Width", dim.width);
		Point point = frmRegexDriver.getLocation();
		myPrefs.putInt("LocX", point.x);
		myPrefs.putInt("LocY", point.y);
		myPrefs.putInt("Divider", splitPane1.getDividerLocation());
		myPrefs.put("Replacement", txtReplacement.getText());

		int regexCount = regexCodeModel.getSize();
		myPrefs.putInt("regexCount", regexCount);
		for (int i = 0; i < regexCount; i++) {
			myPrefs.put("regexCode_" + i, (String) regexCodeModel.getElementAt(i));
		} // for

		int sourceCount = sourceStringModel.getSize();
		myPrefs.putInt("sourceCount", sourceCount);
		for (int i = 0; i < sourceCount; i++) {
			myPrefs.put("sourceString_" + i, (String) sourceStringModel.getElementAt(i));
		} // for

		myPrefs = null;
		System.exit(0);
	}// appClose

	private void appInit() {
		Preferences myPrefs = Preferences.userNodeForPackage(RegexDriver.class).node(this.getClass().getSimpleName());

		frmRegexDriver.setSize(myPrefs.getInt("Width", 1287),myPrefs.getInt("Height", 477));
//		frmRegexDriver.setSize(myPrefs.getInt("Width", 1287),myPrefs.getInt("Height", 477));

		
		frmRegexDriver.setLocation(myPrefs.getInt("LocX", 100), myPrefs.getInt("LocY", 100));
		splitPane1.setDividerLocation(myPrefs.getInt("Divider", 250));
		txtReplacement.setText(myPrefs.get("Replacement", "<nothing>"));

		int regexCount = myPrefs.getInt("regexCount", 0);
		for (int i = 0; i < regexCount; i++) {
			regexCodeModel.addElement(myPrefs.get("regexCode_" + i, "<<exception>>"));
		} // for

		int sourceCount = myPrefs.getInt("sourceCount", 0);
		for (int i = 0; i < sourceCount; i++) {
			sourceStringModel.addElement(myPrefs.get("sourceString_" + i, "<<exception>>"));
		} // for
		myPrefs = null;

		doc = tpResult.getStyledDocument();
		setAttributes();
		
		log.setDoc(txtLog.getStyledDocument());
		log.addTimeStamp("Starting Regex Drive: ");

		cbRegexCode.setModel(regexCodeModel);
		cbSourceString.setModel(sourceStringModel);

		setupPopupMenus();
		txtLog1.setText("Right click on this log \r\npane  to print or clear\r\ncontents.\r\n\r\nRight Click on either\r\nRegex Code or Source\r\nString to bring up\r\noption to delete the\r\ncurrently displayed value\r\n");
	}// appInit

	public RegexDriver() {
		initialize();
		appInit();
	}// Constructor

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmRegexDriver = new JFrame();
		frmRegexDriver.setIconImage(Toolkit.getDefaultToolkit().getImage(RegexDriver.class.getResource("Regex.jpg")));

		frmRegexDriver.setTitle("Regex Driver  Ver 2.0");
		//frmRegexDriver.setBounds(100, 100, 450, 300);
		frmRegexDriver.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 25, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		frmRegexDriver.getContentPane().setLayout(gridBagLayout);

		splitPane1 = new JSplitPane();
		splitPane1.setPreferredSize(new Dimension(0, 0));
		splitPane1.setMinimumSize(new Dimension(0, 0));
		GridBagConstraints gbc_splitPane1 = new GridBagConstraints();
		gbc_splitPane1.insets = new Insets(0, 0, 5, 0);
		gbc_splitPane1.fill = GridBagConstraints.BOTH;
		gbc_splitPane1.gridx = 0;
		gbc_splitPane1.gridy = 1;
		frmRegexDriver.getContentPane().add(splitPane1, gbc_splitPane1);

		JPanel panelLeft = new JPanel();
		panelLeft.setPreferredSize(new Dimension(0, 0));
		panelLeft.setMinimumSize(new Dimension(0, 0));
		splitPane1.setLeftComponent(panelLeft);
		GridBagLayout gbl_panelLeft = new GridBagLayout();
		gbl_panelLeft.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panelLeft.rowHeights = new int[] { 0, 0, 0, 0, 0, 10, 0, 0, 0, 25, 0, 30, 0 };
		gbl_panelLeft.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panelLeft.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		panelLeft.setLayout(gbl_panelLeft);

		JLabel lblRegexCode = new JLabel("Regex Code");
		GridBagConstraints gbc_lblRegexCode = new GridBagConstraints();
		gbc_lblRegexCode.insets = new Insets(0, 0, 5, 5);
		gbc_lblRegexCode.gridx = 0;
		gbc_lblRegexCode.gridy = 1;
		panelLeft.add(lblRegexCode, gbc_lblRegexCode);

		cbRegexCode = new JComboBox<String>();
		cbRegexCode.addItemListener(adapterForRegexDriver);
		cbRegexCode.setName(CB_REGEX_CODE);
		cbRegexCode.setEditable(true);
		cbRegexCode.setFont(new Font("Courier New", Font.PLAIN, 18));
		GridBagConstraints gbc_cbRegexCode = new GridBagConstraints();
		gbc_cbRegexCode.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbRegexCode.anchor = GridBagConstraints.NORTH;
		gbc_cbRegexCode.insets = new Insets(0, 0, 5, 5);
		gbc_cbRegexCode.gridx = 1;
		gbc_cbRegexCode.gridy = 1;
		panelLeft.add(cbRegexCode, gbc_cbRegexCode);

		JLabel lblSourceString = new JLabel("  Source String");
		GridBagConstraints gbc_lblSourceString = new GridBagConstraints();
		gbc_lblSourceString.insets = new Insets(0, 0, 5, 5);
		gbc_lblSourceString.gridx = 0;
		gbc_lblSourceString.gridy = 3;
		panelLeft.add(lblSourceString, gbc_lblSourceString);

		cbSourceString = new JComboBox<String>();
		cbSourceString.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
		cbSourceString.addItemListener(adapterForRegexDriver);
		cbSourceString.setName(CB_SOURCE_STRING);
		cbSourceString.setEditable(true);
		cbSourceString.setFont(new Font("Courier New", Font.PLAIN, 18));
		GridBagConstraints gbc_cbSourceString = new GridBagConstraints();
		gbc_cbSourceString.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbSourceString.insets = new Insets(0, 0, 5, 5);
		gbc_cbSourceString.gridx = 1;
		gbc_cbSourceString.gridy = 3;
		panelLeft.add(cbSourceString, gbc_cbSourceString);
		
		JLabel lblResult = new JLabel("  Result");
		GridBagConstraints gbc_lblResult = new GridBagConstraints();
		gbc_lblResult.anchor = GridBagConstraints.WEST;
		gbc_lblResult.insets = new Insets(0, 0, 5, 5);
		gbc_lblResult.gridx = 0;
		gbc_lblResult.gridy = 4;
		panelLeft.add(lblResult, gbc_lblResult);

		tpResult = new JTextPane();
		tpResult.setEditable(false);
		tpResult.setFont(new Font("Courier New", Font.BOLD, 18));
		GridBagConstraints gbc_tpResult = new GridBagConstraints();
		gbc_tpResult.insets = new Insets(0, 0, 5, 5);
		gbc_tpResult.fill = GridBagConstraints.BOTH;
		gbc_tpResult.gridx = 1;
		gbc_tpResult.gridy = 4;
		panelLeft.add(tpResult, gbc_tpResult);

		JPanel panel_2 = new JPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(0, 0, 5, 5);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 1;
		gbc_panel_2.gridy = 6;
		panelLeft.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_2.rowHeights = new int[] { 0, 0 };
		gbl_panel_2.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_2.setLayout(gbl_panel_2);

		JLabel lblGroupStart = new JLabel("Group Boundary:");
		GridBagConstraints gbc_lblGroupStart = new GridBagConstraints();
		gbc_lblGroupStart.anchor = GridBagConstraints.EAST;
		gbc_lblGroupStart.insets = new Insets(0, 0, 0, 5);
		gbc_lblGroupStart.gridx = 2;
		gbc_lblGroupStart.gridy = 0;
		panel_2.add(lblGroupStart, gbc_lblGroupStart);

		lblGroupBoundary = new JLabel(" ~ - ~");
		GridBagConstraints gbc_lblGroupBoundary = new GridBagConstraints();
		gbc_lblGroupBoundary.insets = new Insets(0, 0, 0, 5);
		gbc_lblGroupBoundary.anchor = GridBagConstraints.EAST;
		gbc_lblGroupBoundary.gridx = 6;
		gbc_lblGroupBoundary.gridy = 0;
		panel_2.add(lblGroupBoundary, gbc_lblGroupBoundary);

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 7;
		panelLeft.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 25, 0, 0, 0, 25, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JButton btnMatches = new JButton("Matches");
		btnMatches.setToolTipText("Attempts to match the entire region against the pattern.");
		btnMatches.addActionListener(adapterForRegexDriver);
		btnMatches.setActionCommand(BTN_MATCHES);
		GridBagConstraints gbc_btnMatches = new GridBagConstraints();
		gbc_btnMatches.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnMatches.insets = new Insets(0, 0, 0, 5);
		gbc_btnMatches.gridx = 4;
		gbc_btnMatches.gridy = 0;
		panel.add(btnMatches, gbc_btnMatches);

		JButton btnFind = new JButton("Find");
		btnFind.setToolTipText("Attempts to find the next subsequence of the input sequence that matches the pattern.");
		btnFind.addActionListener(adapterForRegexDriver);
		btnFind.setActionCommand(BTN_FIND);
		GridBagConstraints gbc_btnFind = new GridBagConstraints();
		gbc_btnFind.insets = new Insets(0, 0, 0, 5);
		gbc_btnFind.fill = GridBagConstraints.BOTH;
		gbc_btnFind.gridx = 0;
		gbc_btnFind.gridy = 0;
		panel.add(btnFind, gbc_btnFind);

		btnFindNext = new JButton("FindNext");
		btnFindNext.setEnabled(false);
		btnFindNext.setToolTipText(
				"Attempts to find the next subsequence of the input sequence that matches the pattern.");
		btnFindNext.addActionListener(adapterForRegexDriver);
		btnFindNext.setActionCommand(BTN_FIND_NEXT);
		GridBagConstraints gbc_btnFindNext = new GridBagConstraints();
		gbc_btnFindNext.insets = new Insets(0, 0, 0, 5);
		gbc_btnFindNext.gridx = 2;
		gbc_btnFindNext.gridy = 0;
		panel.add(btnFindNext, gbc_btnFindNext);

		JButton btnLookingAt = new JButton("Looking At");
		btnLookingAt.setToolTipText(
				"Attempts to match the input sequence, starting at the beginning of the region, against the pattern.");
		btnLookingAt.addActionListener(adapterForRegexDriver);
		btnLookingAt.setActionCommand(BTN_LOOKING_AT);
		GridBagConstraints gbc_btnLookingAt = new GridBagConstraints();
		gbc_btnLookingAt.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnLookingAt.gridx = 6;
		gbc_btnLookingAt.gridy = 0;
		panel.add(btnLookingAt, gbc_btnLookingAt);

		Component verticalStrut = Box.createVerticalStrut(20);
		verticalStrut.setPreferredSize(new Dimension(0, 40));
		verticalStrut.setMinimumSize(new Dimension(0, 40));
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut.gridx = 0;
		gbc_verticalStrut.gridy = 9;
		panelLeft.add(verticalStrut, gbc_verticalStrut);

		JPanel panel_3 = new JPanel();
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.insets = new Insets(0, 0, 5, 5);
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 1;
		gbc_panel_3.gridy = 10;
		panelLeft.add(panel_3, gbc_panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panel_3.rowHeights = new int[] { 0, 0 };
		gbl_panel_3.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel_3.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_3.setLayout(gbl_panel_3);

		JButton btnReplaceFirst = new JButton("Replace First");
		btnReplaceFirst.setToolTipText(
				"Replaces the first subsequence of the input sequence that matches the pattern with the given replacement string.");
		GridBagConstraints gbc_btnReplaceFirst = new GridBagConstraints();
		gbc_btnReplaceFirst.insets = new Insets(0, 0, 0, 5);
		gbc_btnReplaceFirst.gridx = 0;
		gbc_btnReplaceFirst.gridy = 0;
		panel_3.add(btnReplaceFirst, gbc_btnReplaceFirst);
		btnReplaceFirst.addActionListener(adapterForRegexDriver);
		btnReplaceFirst.setActionCommand("btnReplaceFirst");

		JButton btnReplaceAll = new JButton("ReplaceAll");
		btnReplaceAll.setToolTipText(
				"Replaces every subsequence of the input sequence that matches the pattern with the given replacement string.");
		btnReplaceAll.addActionListener(adapterForRegexDriver);
		btnReplaceAll.setActionCommand("btnReplaceAll");
		GridBagConstraints gbc_btnReplaceAll = new GridBagConstraints();
		gbc_btnReplaceAll.insets = new Insets(0, 0, 0, 5);
		gbc_btnReplaceAll.gridx = 2;
		gbc_btnReplaceAll.gridy = 0;
		panel_3.add(btnReplaceAll, gbc_btnReplaceAll);

		txtReplacement = new JTextField();
		GridBagConstraints gbc_txtReplacement = new GridBagConstraints();
		gbc_txtReplacement.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtReplacement.gridx = 4;
		gbc_txtReplacement.gridy = 0;
		panel_3.add(txtReplacement, gbc_txtReplacement);
		txtReplacement.setFont(new Font("Courier New", Font.PLAIN, 18));
		txtReplacement.setColumns(10);
		
//				JButton btnActOnResult = new JButton("Act On Result");
//				GridBagConstraints gbc_btnActOnResult = new GridBagConstraints();
//				gbc_btnActOnResult.insets = new Insets(0, 0, 5, 5);
//				gbc_btnActOnResult.gridx = 1;
//				gbc_btnActOnResult.gridy = 0;
//				panel_1.add(btnActOnResult, gbc_btnActOnResult);
//				btnActOnResult.addActionListener(adapterForRegexDriver);
//				btnActOnResult.setActionCommand("btnActOnResult");

		JPanel panelRight = new JPanel();
		panelRight.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		splitPane1.setRightComponent(panelRight);
		GridBagLayout gbl_panelRight = new GridBagLayout();
		gbl_panelRight.columnWidths = new int[] { 0, 50, 0 };
		gbl_panelRight.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelRight.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_panelRight.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		panelRight.setLayout(gbl_panelRight);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panelRight.add(scrollPane, gbc_scrollPane);

		txtLog1 = new JTextArea();
//		txtLog.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseClicked(MouseEvent me) {
//				if (me.getClickCount() >= 2) {
//					if (me.getComponent().getName() == "txtLog") {
//						((JTextComponent) me.getComponent()).setText("");
//					} // inner if
//				} // outer if
//			}
//		});
		txtLog1.setName("txtLog");
		scrollPane.setViewportView(txtLog1);
				
				JScrollPane scrollPane_1 = new JScrollPane();
				GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
				gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
				gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
				gbc_scrollPane_1.gridx = 1;
				gbc_scrollPane_1.gridy = 0;
				panelRight.add(scrollPane_1, gbc_scrollPane_1);
				
				JScrollPane scrollPane_2 = new JScrollPane();
				scrollPane_1.setViewportView(scrollPane_2);
				
				txtLog = new JTextPane();
				scrollPane_2.setViewportView(txtLog);
		
				btnSaveLog = new JButton("Save Log");
				GridBagConstraints gbc_btnSaveLog = new GridBagConstraints();
				gbc_btnSaveLog.insets = new Insets(0, 0, 0, 5);
				gbc_btnSaveLog.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnSaveLog.gridx = 0;
				gbc_btnSaveLog.gridy = 1;
				panelRight.add(btnSaveLog, gbc_btnSaveLog);
				btnSaveLog.setToolTipText("Nothing saved yet");
				btnSaveLog.addActionListener(adapterForRegexDriver);
				btnSaveLog.setActionCommand("btnSaveLog");
		splitPane1.setDividerLocation(150);

		JPanel panelStatus = new JPanel();
		panelStatus.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelStatus = new GridBagConstraints();
		gbc_panelStatus.fill = GridBagConstraints.BOTH;
		gbc_panelStatus.gridx = 0;
		gbc_panelStatus.gridy = 2;
		frmRegexDriver.getContentPane().add(panelStatus, gbc_panelStatus);

		JMenuBar menuBar = new JMenuBar();
		frmRegexDriver.setJMenuBar(menuBar);

		frmRegexDriver.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				appClose();
			}
		});
	}// initialize

	private JFrame frmRegexDriver;
	private JSplitPane splitPane1;
	private JTextArea txtLog1;
	private JComboBox<String> cbRegexCode;
	private JComboBox<String> cbSourceString;
	private JTextField txtReplacement;
	
	/*          POP UP MENU                 */
	
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent) {
				if (mouseEvent.isPopupTrigger()) {
					showMenu(mouseEvent);
				} // if popup Trigger
			}// mousePressed

			public void mouseReleased(MouseEvent mouseEvent) {
				if (mouseEvent.isPopupTrigger()) {
					showMenu(mouseEvent);
				} // if
			}// mouseReleased

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}// showMenu
		});
	}// addPopup
	private void doLogClear() {
			txtLog1.setText("");
	}// doLogClear

	private void doLogPrint() {
		Font originalFont = txtLog1.getFont();
		try {
			String header = "RegexDriver Log";
			txtLog1.setFont(originalFont.deriveFont(8.0f));
			MessageFormat headerMessage = new MessageFormat(header);
			MessageFormat footerMessage = new MessageFormat(new Date().toString() + "           Page - {0}");
			txtLog1.print(headerMessage, footerMessage);
			// textPane.setFont(new Font("Courier New", Font.PLAIN, 14));
			txtLog1.setFont(originalFont);
		} catch (PrinterException e) {
			txtLog1.setFont(originalFont);
			postLogMessage("java.awt.print.PrinterAbortException");
			System.err.println("java.awt.print.PrinterAbortException");
			e.printStackTrace();
		} // try
	}// doLogPrint
	/*          POP UP MENU                 */


	public class AdapterForRegexDriver implements ActionListener, ItemListener {

		/* ActionListener */
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String actionCommand = actionEvent.getActionCommand();
			switch (actionCommand) {
			case BTN_MATCHES:
				doMatch();
				break;
			case BTN_FIND:
				doFind();
				break;
			case BTN_FIND_NEXT:
				doFindNext();
				break;
			case BTN_LOOKING_AT:
				doLookingAt();
				break;
			case BTN_SAVE_LOG:
				saveLog();
				break;
			case BTN_REPLACE_FIRST:
				doReplace(false);
				break;
			case BTN_REPLACE_ALL:
				doReplace(true);
				break;

			case MNU_POP_REMOVE_SOURCE:
			case MNU_POP_REMOVE_REGEX:
				doRemoveFromList(actionEvent);
				break;
				
			case PUM_LOG_PRINT:
				doLogPrint();
				break;
			case PUM_LOG_CLEAR:
				doLogClear();
				break;

			default:
				System.err.printf("Unknown Action Command %s.%n", actionCommand);
				break;
			}// switch
		}// actionPerformed

		/* ItemListener */

		@Override
		public void itemStateChanged(ItemEvent itemEvent) {
			@SuppressWarnings("unchecked")
			JComboBox<String> source = (JComboBox<String>) itemEvent.getSource();
			DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) source.getModel();
			Object object = itemEvent.getItem();
			if (model.getIndexOf(object) == -1) {
				model.insertElementAt((String) object, 0);
			} // if new

		}// itemStateChanged

	}// class AdapterForRegexDriver CB_REGEX_CODE

	private static final String CB_REGEX_CODE = "cbRegexCode";
	private static final String CB_SOURCE_STRING = "cbSourceString";

	private static final String MNU_POP_REMOVE_REGEX = "mnuPopRemoveRegex";
	private static final String MNU_POP_REMOVE_SOURCE = "mnuPopRemoveSource";

	private static final String BTN_MATCHES = "btnMatches";
	private static final String BTN_FIND = "btnFind";
	private static final String BTN_FIND_NEXT = "btnFindNext";
	private static final String BTN_LOOKING_AT = "btnLookingAt";
	private static final String BTN_SAVE_LOG = "btnSaveLog";
	private static final String BTN_REPLACE_FIRST = "btnReplaceFirst";
	private static final String BTN_REPLACE_ALL = "btnReplaceAll";
	
	private static final String PUM_LOG_PRINT = "popupLogPrint";
	private static final String PUM_LOG_CLEAR = "popupLogClear";


	// private static final String EMPTY_STRING = "";
	private JTextPane tpResult;
	private JLabel lblGroupBoundary;
	private JButton btnFindNext;
	private JButton btnSaveLog;
	private JTextPane txtLog;

}// class GUItemplate