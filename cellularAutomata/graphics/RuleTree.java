/*
 RuleTree -- a class within the Cellular Automaton Explorer. 
 Copyright (C) 2005  David B. Bahr (http://academic.regis.edu/dbahr/)

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package cellularAutomata.graphics;

import java.awt.Component;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import cellularAutomata.CAConstants;
import cellularAutomata.CurrentProperties;
import cellularAutomata.lattice.StandardOneDimensionalLattice;
import cellularAutomata.reflection.ReflectionTool;
import cellularAutomata.reflection.RuleHash;
import cellularAutomata.reflection.URLResource;
import cellularAutomata.rules.Rule;
import cellularAutomata.rules.util.RuleFolderNames;
import cellularAutomata.util.CAToolTip;

/**
 * Creates a tree for selecting CA rules. Each rule may appear in many folders.
 * The rule name and folders are specified by the rule.
 * 
 * @author David Bahr
 */
public class RuleTree
{
	// A tool tip used when the rule is not available with the selected lattice
	private static final String NOT_AVAILABLE_WITH_LATTICE_TOOLTIP = "Not available "
			+ "with the selected lattice.";

	// The name of the folder where recently selected rules are listed
	private static final String RECENTLY_SELECTED_FOLDER = "Recently Selected Rules";

	// The name of the root (top) folder in the menu
	private static final String ROOT_FOLDER_NAME = "CA Rules";

	// the tree node/folder that contains "All Rules"
	private DefaultMutableTreeNode allRulesFolderNode = null;

	// the tree node that contains the currently active rule's node (the active
	// one at the time this class is constructed -- it is not kept up to date
	// after that).
	private DefaultMutableTreeNode activeRuleAtStartUpNode = null;

	// the node on the tree that is currently selected
	// private DefaultMutableTreeNode currentlySelectedNode = null;

	// the tree node that contains the "Life" rule
	private DefaultMutableTreeNode lifeNode = null;

	// the tree node/folder that contains "Recently Selected Rules"
	private DefaultMutableTreeNode recentlySelectedFolderNode = null;

	// the tree node that contains the "Rule 102" rule
	private DefaultMutableTreeNode rule102Node = null;

	// the tree model
	private DefaultTreeModel treeModel = null;

	// a list of which rules (String) are enabled and disabled (Boolean)
	private Hashtable<String, Boolean> enabledRuleHash = new Hashtable<String, Boolean>();

	// the scroll pane that holds the JTree
	private JScrollPane treeScrollPane = null;

	// the tree of rules
	private JTree tree = null;

	// the currently selected lattice (not the active one, but the SELECTED one)
	private String currentlySelectedLattice = null;

	// the currently active rule at the time this class is constructed (this is
	// NOT the currently selected rule which may change -- this value is not
	// kept up to date)
	private String activeRuleAtStartUp = null;

	/**
	 * Creates a menu tree holding each of the CA rules.
	 */
	public RuleTree(MouseListener listener)
	{
		super();

		if(currentlySelectedLattice == null)
		{
			currentlySelectedLattice = CurrentProperties.getInstance()
					.getLatticeDisplayName();
		}

		if(activeRuleAtStartUp == null)
		{
			String ruleClassName = CurrentProperties.getInstance()
					.getRuleClassName();
			Rule rule = ReflectionTool
					.instantiateMinimalRuleFromClassName(ruleClassName);
			activeRuleAtStartUp = rule.getDisplayName();
		}

		// The top/root folder of the tree.
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(
				ROOT_FOLDER_NAME);

		// the tree itself
		treeModel = new DefaultTreeModel(top);
		tree = new JTree(treeModel)
		{
			/**
			 * Override the createToolTip method in the JTree so that it can
			 * change to a warning color when a node is disabled.
			 */
			public JToolTip createToolTip()
			{
				CAToolTip caToolTip = new CAToolTip(this);

				CARuleTreeCellRenderer renderer = (CARuleTreeCellRenderer) this
						.getCellRenderer();

				if((renderer != null)
						&& (renderer.getToolTipText() != null)
						&& renderer.getToolTipText().contains(
								NOT_AVAILABLE_WITH_LATTICE_TOOLTIP))
				{
					caToolTip.setToWarningColor();
				}

				return caToolTip;
			}
		};
		treeScrollPane = new JScrollPane(tree);

		// tree.setPreferredSize(new Dimension(50, 50));

		// Allow tool tips on the tree cells
		ToolTipManager.sharedInstance().registerComponent(tree);

		// add all of the rules and folders to the tree
		createTreeNodes(top);

		// Set an appropriate icon and tooltip for each CA rule.
		CARuleTreeCellRenderer renderer = new CARuleTreeCellRenderer();
		tree.setCellRenderer(renderer);

		// set the currently active rule. This should have been done in the
		// method createTreeNodes(), so this is a backup.
		if(activeRuleAtStartUpNode == null)
		{
			// may not have selected anything, so use the default
			String lattice = CurrentProperties.getInstance()
					.getLatticeDisplayName();
			if(lattice.equals(StandardOneDimensionalLattice.DISPLAY_NAME))
			{
				activeRuleAtStartUpNode = rule102Node;
			}
			else
			{
				activeRuleAtStartUpNode = lifeNode;
			}
		}

		// now add the currently active rule to the "recently selected"
		// folder in the rule tree menu (actually, add a copy so it doesn't
		// get deleted from its other location).
		String ruleName = activeRuleAtStartUpNode.toString();
		addToRecentlySelectedRulesFolder(ruleName);

		// select the currently active rule in the "recently selected"
		// folder and scroll to it. Note: when first instantiated, there is
		// only one child in the recentlySelectedFolderNode (the one we
		// added above).
		DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) recentlySelectedFolderNode
				.getFirstChild();
		TreePath childPath = new TreePath(childNode.getPath());
		tree.setSelectionPath(childPath);
		tree.scrollPathToVisible(childPath);

		// Add a listener. This has to happen AFTER setting the path.
		// Otherwise get a race condition (tree calls listener which calls
		// calls tree which calls listener...).
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		// tree.addTreeSelectionListener(listener);
		tree.addMouseListener(listener);
	}

	/**
	 * Adds each of the rules to the tree. They are added to the default "All
	 * rules" folder and any other folders specified by the rule.
	 * 
	 * @param top
	 *            The top or root of the tree.
	 * @return a scrollpane holding the JTree.
	 */
	private void createTreeNodes(DefaultMutableTreeNode top)
	{
		// keep a list of the folders. This is faster than going through an
		// enumeration of the tree every time I need just the folders (and not
		// the leafs).
		LinkedList<DefaultMutableTreeNode> folderList = new LinkedList<DefaultMutableTreeNode>();

		// add all folders to the tree
		String[] folderNames = getRuleFolders();

		for(int i = 0; i < folderNames.length; i++)
		{
			DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(
					folderNames[i]);

			if(folderNames[i].equals(RuleFolderNames.ALL_RULES_FOLDER))
			{
				// save this for later (so we can expand the tree)
				allRulesFolderNode = folderNode;
			}

			if(folderNames[i].equals(RECENTLY_SELECTED_FOLDER))
			{
				// save this for later (so we can add rules to it)
				recentlySelectedFolderNode = folderNode;
			}

			// add the folder to the tree
			top.add(folderNode);

			// add this new folder to the list
			folderList.add(folderNode);
		}

		// add all rules to the folders
		String[] ruleNames = getRuleNames();
		Hashtable<String, List<String>> foldersForEachRuleHash = getAllFoldersForEachRule();
		for(String ruleName : ruleNames)
		{
			// to which folders do I add this rule?
			List<String> folders = foldersForEachRuleHash.get(ruleName);

			// loop over all the folder names and add to the appropriate folders
			// on the tree
			for(String folderName : folders)
			{
				// add to the correct tree folders
				for(DefaultMutableTreeNode folderNode : folderList)
				{
					if(folderNode.toString().equals(folderName))
					{
						// The folderName matches the tree, so add a node for
						// the rule. Must create a new node for each folder to
						// which the rule is added.
						DefaultMutableTreeNode ruleNode = new DefaultMutableTreeNode(
								ruleName);
						folderNode.add(ruleNode);

						// save the currently selected rule's tree node so we
						// can expand to it later. Only do this in the first
						// time because the rule may appear in many folders.
						if(this.activeRuleAtStartUp.equals(ruleName)
								&& (activeRuleAtStartUpNode == null))
						{
							activeRuleAtStartUpNode = ruleNode;
						}

						// save the Rule102 node so we can expand to it later.
						// Only do this in the first time because the rule may
						// appear in many folders.
						if(ruleName.contains("Rule 102")
								&& (rule102Node == null))
						{
							rule102Node = ruleNode;
						}

						// save the Life node so we can expand to it later.
						// Only do this in the first time because the rule may
						// appear in many folders.
						if(ruleName.equals("Life") && (lifeNode == null))
						{
							lifeNode = ruleNode;
						}
					}
				}
			}
		}
	}

	/**
	 * Creates a hash table with rule descriptions as keys and a list of the
	 * folder names as values.
	 * 
	 * @return the folder names associated with the rule.
	 */
	private Hashtable<String, List<String>> getAllFoldersForEachRule()
	{
		// the hash table that is returned
		Hashtable<String, List<String>> foldersForEachRuleHash = new Hashtable<String, List<String>>();

		// all the possible rules
		RuleHash ruleHash = new RuleHash();
		Iterator values = ruleHash.valuesIterator();

		// now get the display folders for this rule
		while(values.hasNext())
		{
			String nextRule = (String) values.next();

			// instantiate the rule using reflection
			Rule rule = ReflectionTool
					.instantiateMinimalRuleFromClassName(nextRule);

			// make sure it really was instantiated!
			if(rule != null)
			{
				// now get the display and folder names
				String displayName = rule.getDisplayName();
				String[] folderNames = rule.getDisplayFolderNames();

				// make sure that we add the default "all rules" folder to this
				// list
				LinkedList<String> folderList = new LinkedList<String>();
				if(folderNames == null || folderNames.length == 0)
				{
					folderList.add(RuleFolderNames.ALL_RULES_FOLDER);
				}
				else
				{
					for(String folder : folderNames)
					{
						folderList.add(folder);
					}

					folderList.add(RuleFolderNames.ALL_RULES_FOLDER);
				}

				// if the rule class was contributed by a user and is in the
				// userRules class folder, add it to "user rules" tree folder as
				// well
				if(rule.getClass().toString().contains("userRules")) // rule.getClass().getPackage().getName().contains("userRules")
				{
					folderList.add(RuleFolderNames.USER_RULES_FOLDER);
				}

				if(folderList != null && folderList.size() > 0)
				{
					foldersForEachRuleHash.put(displayName, folderList);
				}
			}
		}

		return foldersForEachRuleHash;
	}

	/**
	 * Gets all the lattices compatible with the specified rule. Returns null if
	 * the rule will work with all lattices.
	 * 
	 * @param ruleName
	 *            The descriptive name of the rule.
	 * @return names of lattices that will work with this rule, or null if it
	 *         will work with all lattices.
	 */
	private String[] getCompatibleLattices(String ruleName)
	{
		// the compatible lattices
		String[] compatibleLattices = null;

		// instantiate the rule using reflection
		RuleHash ruleHash = new RuleHash();
		String ruleClassName = ruleHash.get(ruleName);
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(ruleClassName);

		if(rule != null)
		{
			// get the compatible lattices
			compatibleLattices = rule.getCompatibleLattices();
		}

		return compatibleLattices;
	}

	/**
	 * Each rule may live in one or more folders, and this gets all of those
	 * folder names. In other words, this gets all possible folder names (by
	 * looking at the folders desired by all possible rules, and then adding
	 * some defaults like ALL_RULES_FOLDER, USER_RULE_FOLDER, etc.).
	 * 
	 * @return An alphabetized list of folders into which the rules will be
	 *         placed.
	 */
	private String[] getRuleFolders()
	{
		// get a list of all the folders for each rule
		Hashtable<String, List<String>> allFoldersForEachRule = getAllFoldersForEachRule();
		Collection<List<String>> folderList = allFoldersForEachRule.values();

		// go through the collection and get all of the names. The names will be
		// repeated many times, so ignore repetitions.
		HashSet<String> setOfFolderNames = new HashSet<String>();
		for(List<String> folderNames : folderList)
		{
			// get the folder names
			for(String name : folderNames)
			{
				setOfFolderNames.add(name);
			}
		}

		// all rules live in the default folder.
		setOfFolderNames.add(RuleFolderNames.ALL_RULES_FOLDER);

		// recently used rules live in the "Recently selected rules"
		// folder.
		setOfFolderNames.add(RECENTLY_SELECTED_FOLDER);

		// some rules are created by contributers and live in the "user rules"
		// folder.
		setOfFolderNames.add(RuleFolderNames.USER_RULES_FOLDER);

		// convert to an alphabetized array
		String[] folderNames = setOfFolderNames.toArray(new String[1]);
		Arrays.sort(folderNames);

		return folderNames;
	}

	/**
	 * Gets an icon from the rule.
	 * 
	 * @param ruleName
	 *            The display name (description) of the rule.
	 * @return the image icon associated with the specified rule.
	 */
	private ImageIcon getRuleIcon(String ruleName)
	{
		// the icon that will be returned
		ImageIcon ruleIcon = null;

		// all the possible rules
		RuleHash ruleHash = new RuleHash();

		// get the rule's class
		String ruleClassName = ruleHash.get(ruleName);

		// instantiate the rule using reflection
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(ruleClassName);

		// make sure it really was instantiated!
		if(rule != null)
		{
			ruleIcon = rule.getDisplayIcon();
		}

		// if there isn't an icon, then use the default
		if(ruleIcon == null)
		{
			// get the CA icon image URL (searches the classpath to find the
			// image file).
			URL ruleIconUrl = URLResource.getResource(Rule.DEFAULT_ICON_PATH);
			if(ruleIconUrl != null)
			{
				ruleIcon = new ImageIcon(ruleIconUrl);
			}
		}

		return ruleIcon;
	}

	/**
	 * All of the available CA rules.
	 * 
	 * @return All of the CA rules.
	 */
	private String[] getRuleNames()
	{
		// will be a list of the rules compatible with this lattice
		// depends on the CellState returned by the rule.
		LinkedList<String> ruleList = new LinkedList<String>();

		// all the possible rules
		RuleHash ruleHash = new RuleHash();
		Iterator values = ruleHash.valuesIterator();

		// now see which ones are compatible with the lattice
		while(values.hasNext())
		{
			String nextRule = (String) values.next();

			// instantiate the rule using reflection
			Rule rule = ReflectionTool
					.instantiateMinimalRuleFromClassName(nextRule);

			// make sure it really was instantiated!
			if(rule != null)
			{
				ruleList.add(rule.getDisplayName());
			}
		}

		// convert to an array
		String[] rules = ruleList.toArray(new String[1]);

		// sort the array (looks better)
		Arrays.sort(rules);

		return rules;
	}

	/**
	 * Gets a tool tip from the rule.
	 * 
	 * @param ruleName
	 *            The display name (description) of the rule.
	 * @return the tool tip associated with the specified rule.
	 */
	private String getRuleToolTip(String ruleName)
	{
		// the tool tip that will be returned (null is ok)
		String tooltip = null;

		// all the possible rules
		RuleHash ruleHash = new RuleHash();

		// get the rule's class
		String ruleClassName = ruleHash.get(ruleName);

		// instantiate the rule using reflection
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(ruleClassName);

		// make sure it really was instantiated!
		if(rule != null)
		{
			tooltip = rule.getToolTipDescription();
		}

		return tooltip;
	}

	/**
	 * Decides if the specified rule is compatible with the specified lattice.
	 * 
	 * @param ruleName
	 *            The descriptive name of the rule.
	 * @param latticeName
	 *            The descriptive name of the lattice. For example,
	 *            HexagonalLattice.DISPLAY_NAME.
	 * @return true if the specified rule is compatible with the given lattice.
	 */
	public boolean isRuleCompatibleWithLattice(String ruleName,
			String latticeName)
	{
		// the value that will be returned
		boolean isCompatible = false;

		// instantiate the rule using reflection
		RuleHash ruleHash = new RuleHash();
		String ruleClassName = ruleHash.get(ruleName);
		Rule rule = ReflectionTool
				.instantiateMinimalRuleFromClassName(ruleClassName);

		if(rule != null)
		{
			// get the compatible lattices
			String[] compatibleLattices = rule.getCompatibleLattices();

			// if it is null, then it is compatible with every lattice
			if(compatibleLattices == null)
			{
				isCompatible = true;
			}
			else
			{
				for(String lattice : compatibleLattices)
				{
					if(lattice.equals(latticeName))
					{
						isCompatible = true;
					}
				}
			}
		}

		return isCompatible;
	}

	/**
	 * Decides if a node is enabled (true) or disabled (false).
	 * 
	 * @param node
	 *            The node being tested.
	 * @return true if the node is enabled.
	 */
	private boolean nodeIsEnabled(DefaultMutableTreeNode node)
	{
		boolean enabled = false;

		if(node != null)
		{
			enabled = enabledRuleHash.get(node.toString()).booleanValue();
		}

		return enabled;
	}

	/**
	 * Get the rule name that is currently selected (or null if a folder is
	 * selected).
	 * 
	 * @return the rule name (description). Will be null if the user selected a
	 *         folder.
	 */
	public String getSelectedRuleName()
	{
		String ruleName = null;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
				.getLastSelectedPathComponent();

		if(node != null && node.isLeaf() && nodeIsEnabled(node))
		{
			ruleName = node.toString();
		}

		return ruleName;
	}

	/**
	 * Adds the rule to the recently selected rule folder. The rule remains in
	 * this folder only during the currently active CA Explorer session.
	 * 
	 * @param ruleName
	 *            The descriptive name of the rule.
	 */
	public void addToRecentlySelectedRulesFolder(String ruleName)
	{
		// Find out what node is currently selected. If we remove it from the
		// tree (in a moment), then we will have to select/highlight the newly
		// added node (created from ruleName).
		DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) tree
				.getLastSelectedPathComponent();

		// keeps track of whether or not we removed the currently selected node
		// (in the next section of code).
		boolean removedCurrentNode = false;

		// find out if the rule is already present. If so, remove it so we can
		// re-add it to the end of the list.
		Enumeration childEnumeration = recentlySelectedFolderNode
				.breadthFirstEnumeration();
		while(childEnumeration.hasMoreElements())
		{
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) childEnumeration
					.nextElement();
			String childsRuleName = childNode.toString();
			if(childsRuleName.equals(ruleName))
			{
				// are we removing the currently selected rule?
				if(childNode.equals(currentNode))
				{
					removedCurrentNode = true;
				}

				treeModel.removeNodeFromParent(childNode);
			}
		}

		// NOTE: If desired, I can change this so that the new rule is added
		// (below) only if the rule isn't already found above.

		// a new node representing the rule (note that every leaf on the tree
		// has to be a different node, even if they are the same rule --
		// otherwise the old leaf gets removed and placed here instead).
		DefaultMutableTreeNode ruleNode = new DefaultMutableTreeNode(ruleName);

		// now add it to the recently selected folder (do this through the tree
		// model so that it automatically notifies the view to update).
		treeModel.insertNodeInto(ruleNode, recentlySelectedFolderNode,
				recentlySelectedFolderNode.getChildCount());

		// now highlight/select this newly added rule if we just removed the
		// currently selected node
		if(removedCurrentNode)
		{
			TreePath rulePath = new TreePath(ruleNode.getPath());
			tree.setSelectionPath(rulePath);
			tree.scrollPathToVisible(rulePath);
		}
	}

	/**
	 * Gets a tree containing the CA rules, placed inside of a scroll pane.
	 * 
	 * @return the tree containing CA rules
	 */
	public JScrollPane getRuleTreeAsScrollPane()
	{
		return treeScrollPane;
	}

	/**
	 * Get the tree that contains each of the rules.
	 * 
	 * @return the rule tree.
	 */
	public JTree getTree()
	{
		return tree;
	}

	/**
	 * Enables and disables rules depending on the currently selected lattice.
	 * 
	 * @param currentlySelectedLattice
	 *            The lattice currently selected on the Properties panel.
	 */
	public void resetEnabledRules(String currentlySelectedLattice)
	{
		// the cell renderer needs to know which lattice is currently selected
		// on the Properties panel.
		setCurrentLattice(currentlySelectedLattice);

		CARuleTreeCellRenderer renderer = new CARuleTreeCellRenderer();
		tree.setCellRenderer(renderer);
	}

	/**
	 * Lets this class know which lattice is currently selected on the
	 * properties panel (this is not the currently active lattice, just the
	 * selected lattice).
	 * 
	 * @param currentLattice
	 *            the currently selected lattice on the properties panel.
	 */
	public void setCurrentLattice(String currentLattice)
	{
		this.currentlySelectedLattice = currentLattice;
	}

	/**
	 * Sets the specified rule name as the selected CA rule by expanding the
	 * tree elements as necessary. This only sets the value on the tree and does
	 * not "submit" that rule to be the currently active rule.
	 * 
	 * @param ruleName
	 *            The descriptive name of the CA rule.
	 */
	public void setSelectedRule(String ruleName)
	{
		// expand the all rules folder
		tree.expandPath(new TreePath(allRulesFolderNode.getPath()));

		// find the requested node
		TreePath path = tree.getNextMatch(ruleName, 0, Position.Bias.Forward);

		// now select that node
		tree.setSelectionPath(path);
		tree.scrollPathToVisible(path);

		// For better viewing, let's select one row higher than the currently
		// selected rule (note, there is only one row selected, so the array has
		// size 1). Note, don't need to worry about scrolling the last element
		// off of the screen because there are more folders beneath all of the
		// rules in the allRulesFolder.
		int[] rows = tree.getSelectionRows();
		if(rows != null && rows[0] > 1)
		{
			tree.scrollRowToVisible(rows[0] - 1);
		}
	}

	/**
	 * A renderer that lets me tailor the display icon to each individual leaf.
	 * 
	 * @author David Bahr
	 */
	private class CARuleTreeCellRenderer extends DefaultTreeCellRenderer
	{
		// true if the rule is compatible with the selected lattice
		private boolean compatibleWithLattice = true;

		// the icon used by the tree cell
		private Icon icon = null;

		// the tooltip used by the tree cell.
		private String tooltip = null;

		/**
		 * Create a renderer for the tree cell.
		 */
		public CARuleTreeCellRenderer()
		{
			super();
		}

		/**
		 * Gets the component displayed by the renderer for this tree cell, and
		 * specifies the icon and tooltip.
		 */
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus)
		{
			super.getTreeCellRendererComponent(tree, value, sel, expanded,
					leaf, row, hasFocus);

			if(leaf)
			{
				setIconAndToolTip(value);

				// enable this component only if it works with the current
				// lattice
				this.setEnabled(compatibleWithLattice);

				// keep a hash for handy reference -- used in
				// getSelectedRuleName where calling
				// isRuleCompatibleWithLattice() can sometimes lead to a race
				// condition.
				enabledRuleHash.put(value.toString(), new Boolean(
						compatibleWithLattice));

				// these may be null
				this.setIcon(icon);
				if(this.isEnabled())
				{
					this.setToolTipText(tooltip);
				}
				else
				{
					String ruleName = value.toString();
					String[] compatibleLattices = getCompatibleLattices(ruleName);
					String notAvailableToolTip = NOT_AVAILABLE_WITH_LATTICE_TOOLTIP;

					if(compatibleLattices != null
							&& compatibleLattices.length > 0)
					{
						notAvailableToolTip += " Try ";

						if(compatibleLattices.length == 1)
						{
							notAvailableToolTip += compatibleLattices[0] + ".";
						}
						else if(compatibleLattices.length == 2)
						{
							notAvailableToolTip += compatibleLattices[0]
									+ " or " + compatibleLattices[1] + ".";
						}
						else
						{
							notAvailableToolTip += compatibleLattices[0];

							for(int i = 1; i < compatibleLattices.length - 1; i++)
							{
								notAvailableToolTip += ", "
										+ compatibleLattices[i];
							}

							notAvailableToolTip += ", or "
									+ compatibleLattices[compatibleLattices.length - 1]
									+ ".";
						}
					}

					this.setToolTipText(notAvailableToolTip);
				}
			}
			else
			{
				// no tool tip for folders
				this.setToolTipText(null);
			}

			return this;
		}

		/**
		 * Using the rule name, sets the icon and tooltip.
		 * 
		 * @param value
		 *            the cells Rule name.
		 */
		private void setIconAndToolTip(Object value)
		{
			String ruleName = value.toString();
			icon = getRuleIcon(ruleName);
			tooltip = getRuleToolTip(ruleName);

			if(CurrentProperties.getInstance().isFacadeOn())
			{
				// the "easy" facade is turned on, so show all the rules
				compatibleWithLattice = true;
			}
			else if(CAConstants.LATTICE_CENTRIC_CHOICES)
			{
				// only show rules compatible with the selected lattice
				compatibleWithLattice = isRuleCompatibleWithLattice(ruleName,
						currentlySelectedLattice);
			}
			else
			{
				// show all rules all of the time -- this is the default.
				compatibleWithLattice = true;
			}
		}
	}
}
