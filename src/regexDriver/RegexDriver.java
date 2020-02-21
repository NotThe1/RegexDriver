package regexDriver;

/* 
 * uses the appLogger found in project appLogger
 */

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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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

	private void clearResult() {
		try {
			doc.remove(0, doc.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		} // try
	}// clearResult

	private void doMatch() {
		cleanOutput();
		try {
			Pattern pattern = Pattern.compile((String) cbRegexCode.getSelectedItem());
			Matcher matcher = pattern.matcher((CharSequence) cbSourceString.getSelectedItem());

			if (matcher.matches()) {
				log.infof(Color.RED,"%s%n","Match");
				log.infof("start = %d, end = %d%n", matcher.start(), matcher.end());
			} else {
				log.infof(Color.RED,"%s%n","No Match");
				noChange("<< MATCHES - nothing matched >>");
			} // if
		} catch (Exception e) {
			log.errorf("%s - %s%n%n", "In Catch", e.getMessage());
		} // try
		log.addNL();
	}// doMatch

	private void doFind() {
		cleanOutput();
		String sourceText = (String) cbSourceString.getSelectedItem();
		btnFindNext.setEnabled(false);
		patternForFind = Pattern.compile((String) cbRegexCode.getSelectedItem());
		matcherForFind = patternForFind.matcher((CharSequence) cbSourceString.getSelectedItem());
		log.addNL();
		try {
			if (matcherForFind.find()) {
				log.infof(Color.RED,"%s%n","Find");
				log.infof("end = %d, start = %s%n", matcherForFind.end(), matcherForFind.start());
				log.infof("group = |%s|%n", matcherForFind.group());
				log.infof("Before group = |%s|%n", sourceText.substring(0, matcherForFind.start()));
				showGroup(matcherForFind);
				btnFindNext.setEnabled(true);
			} else {
				noChange("<< FIND - nothing found >>");
				log.info("Not Found");
			} // if
		} catch (Exception e) {
			log.errorf("%s - %s%n%n", "In Catch", e.getMessage());
		} // try

	}// doFind

	private void doFindNext() {
		log.addNL();
		if (matcherForFind.hitEnd()) {
			log.info("Matcher has hit end");
			noChange("<< FIND_NEXT - nothing to search >>");
		} else {
			int newStart = matcherForFind.end();
			if (matcherForFind.find(newStart)) {
				String sourceText = (String) cbSourceString.getSelectedItem();

				log.infof(Color.RED,"%s%n","FindNext");
				log.infof("end = %d, start = %s%n", matcherForFind.end(), matcherForFind.start());
				log.infof("group = |%s|%n", matcherForFind.group());
				log.infof("Before group = |%s|%n", sourceText.substring(0, matcherForFind.start()));
				showGroup(matcherForFind);
				btnFindNext.setEnabled(true);
			} else {
				noChange("<< FIND - nothing found >>");
				btnFindNext.setEnabled(false);
			} // inner if - else
		} // outer if - else
	}// doFindNext

	private void doLookingAt() {
		cleanOutput();
		try {
			Pattern pattern = Pattern.compile((String) cbRegexCode.getSelectedItem());
			Matcher matcher = pattern.matcher((CharSequence) cbSourceString.getSelectedItem());

			if (matcher.lookingAt()) {
				log.infof(Color.RED,"%s%n","LookingAt");
				log.infof("start = %d, end = %s%n", matcher.start(), matcher.end());
			} else {
				log.infof(Color.RED,"%s%n","Not LookingAt");
				noChange("<< LOOKING AT - not looking at >>");
			} //
		} catch (Exception e) {
			log.errorf("%s - %s%n%n", "In Catch", e.getMessage());
		} //
		log.addNL();
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

	private void noChange(String message) {
		try {
			doc.remove(0, doc.getLength());
			doc.insertString(0, message, attrRed);
		} catch (BadLocationException e) {
			e.printStackTrace();
		} // try
	}// foundItNot

	private void cleanOutput() {
		lblGroupBoundary.setText(" ~ - ~");
		clearResult();
	}// cleanOutput

	private void showGroup(Matcher matcher) {

		int groupCount = matcher.groupCount();
		log.infof("matcher.groupCount() = %d%n", matcher.groupCount());
		for (int i = 1; i <= groupCount; i++) {
			log.infof("group %d = \"%s\"", i, matcher.group(i));
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
		} // try

	}// showGroup

	private void setAttributes() {
		StyleConstants.setForeground(attrBlack, Color.BLACK);
		StyleConstants.setForeground(attrGreen, Color.GREEN);
		StyleConstants.setForeground(attrRed, Color.RED);
		StyleConstants.setForeground(attrBlue, Color.BLUE);
	}// setAttributes


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
	}// appClose

	private void appInit() {
		Preferences myPrefs = Preferences.userNodeForPackage(RegexDriver.class).node(this.getClass().getSimpleName());

		// frmRegexDriver.setSize(myPrefs.getInt("Width",
		// 1287),myPrefs.getInt("Height",477));
		frmRegexDriver.setSize(myPrefs.getInt("Width", 1287), myPrefs.getInt("Height", 477));

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

		log.setTextPane(txtLog);
		log.setDoc(txtLog.getStyledDocument());
		log.addTimeStamp("Starting Regex Drive: ");
		log.addNL(2);

		cbRegexCode.setModel(regexCodeModel);
		cbSourceString.setModel(sourceStringModel);
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

		frmRegexDriver.setTitle("Regex Driver  Ver 2.5");
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

		JPanel panelRight = new JPanel();
		panelRight.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		splitPane1.setRightComponent(panelRight);
		GridBagLayout gbl_panelRight = new GridBagLayout();
		gbl_panelRight.columnWidths = new int[] { 50, 0 };
		gbl_panelRight.rowHeights = new int[] { 0, 0 };
		gbl_panelRight.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelRight.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelRight.setLayout(gbl_panelRight);

		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 0;
		panelRight.add(scrollPane_1, gbc_scrollPane_1);

		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_1.setViewportView(scrollPane_2);

		txtLog = new JTextPane();
		scrollPane_2.setViewportView(txtLog);

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
			}// windowClosing
		});
	}// initialize

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
	private static final String BTN_REPLACE_FIRST = "btnReplaceFirst";
	private static final String BTN_REPLACE_ALL = "btnReplaceAll";

	private JFrame frmRegexDriver;
	private JSplitPane splitPane1;

	private JComboBox<String> cbRegexCode;
	private JComboBox<String> cbSourceString;
	private JTextField txtReplacement;

	private JTextPane tpResult;
	private JLabel lblGroupBoundary;
	private JButton btnFindNext;
	private JTextPane txtLog;

}// class GUItemplate