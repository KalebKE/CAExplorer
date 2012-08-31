package userRules;

import cellularAutomata.rules.templates.IntegerRuleTemplate;
/**
 * Young versus Old is a rule that will generate a cells value based both on the
 * value of its neighbors and the number of neighbors. See the tool tip for a
 * full description.
 * 
 * @author Alby Graham (edits of description by David Bahr)
 * 
 */
public class YoungVsOld extends IntegerRuleTemplate
{
    /**
     * The name of this rule.
     */
    private static final String RULE_NAME = "Young Versus Old Diamonds";

    // a description of property choices that give the best results for this
    // rule (e.g., which lattice, how many states, etc.)
    private static final String BEST_RESULTS = "<html> <body><b>"
        + RULE_NAME
        + ".</b>"
        + "<p> "
        + "<b>For best results</b>, specify a large number of states (like 200) and use a "
        + "large 200 by 200 square (8 neighbor) "
        + "lattice with a 2 by 2 square initial state.  Other symmetric initial "
        + "states also work well, but don't use a single seed (boring).  "
        + "<p> "
        + "Also try a 5% to 10% random initial state, or draw your own initial state "
        + "(like your initials). "
        + "<p>"
        + "Lattices witrh large numbers of neighbors work better than lattices "
        + "with few neighbors." + leftClickInstructions
        + rightClickInstructions + "</body></html>";

    // a tooltip description for this class
    private String TOOLTIP = "<html> <body><b>"
        + RULE_NAME
        + ".</b> Ever changing colorful diamonds that grow from small seeds.</body></html>";

    /**
     * Creates a new YoungVsOld object with the given properties.
	 * <p>
	 * When calling the parent constructor, the minimalOrLazyInitialization
	 * parameter must be included as shown. The boolean is intended to indicate
	 * when the constructor should build a rule with as small a footprint as
	 * possible. In order to load rules by reflection, the application must
	 * query this class for information like the display name, tooltip
	 * description, etc. At these times it makes no sense to build the complete
	 * rule which may have a large footprint in memory.
	 * <p>
	 * It is recommended that the constructor and instance variables do not
	 * initialize any memory intensive variables and that variables be
	 * initialized only when first needed (lazy initialization). Or all
	 * initializations in the constructor may be placed in an <code>if</code>
	 * statement.
	 * 
	 * <pre>
	 * if(!minimalOrLazyInitialization)
	 * {
	 *     ...initialize
	 * }
	 * </pre>
	 * 
	 * @param minimalOrLazyInitialization
	 *            When true, the constructor instantiates an object with as
	 *            small a footprint as possible. When false, the rule is fully
	 *            constructed. This variable should be passed to the super
	 *            constructor <code>super(minimalOrLazyInitialization);</code>,
	 *            but if uncertain, you may safely ignore this variable.
     */
    public YoungVsOld(boolean minimalOrLazyInitialization)
    {
        super(minimalOrLazyInitialization);
    }

    /**
     * Calculates the value of the cell based on it's neighbors. A cell will die
     * if it has a value and the the number of neighbors with a value greater
     * then cell's value is greater then the number of cells with a value less
     * then or equal to the cell's value (including the cell itself and
     * excluding cells with a value of zero). A cell will be born if it's value
     * is zero and there are two or more cells with the same value in it's
     * neighborhood.
     * 
     * @see cellularAutomata.rules.templates. IntegerRuleTemplate#integerRule(int,
     *      int[], int, int)
     */
    protected int integerRule(int cellValue, int[] neighbors, int numStates,
        int generation)
    {
        // Will only give births on empty cells and will not
        // destroy a newly created cell.
        if(cellValue == 0)
        {
            cellValue = getBirths(neighbors);
        }
        else
        {
            cellValue = checkDeath(cellValue, neighbors);
        }

        // Make sure that the value is not greater the the number
        // of states.
        // May want to make this 1.
        if(cellValue >= numStates)
        {
            cellValue = 1;
        }
        return cellValue;
    }

    /**
     * Checks to see if a cell will die. The cell's value will be set to zero if
     * it dies and will keeps its original value if it lived.
     * 
     * @param cellValue
     *            The value of the cell.
     * 
     * @param neighbors
     *            The cells in it's neighborhood.
     * 
     * @return A 0 if the cell died or the value of the cell if it lived.
     */
    private int checkDeath(int cellValue, int[] neighbors)
    {
        // Die if the number of "older" cells is greater then
        // the number of the "younger" cells.
        // NOTE FROM DAVE: The cell itself is counted as a
        // younger cell.
        int numberOfNumberOfYoungerCells = 1;
        int numberOfNumberOfOlderCells = 0;
        for(int i = 0; i < neighbors.length; i++)
        {
            // We don't want to count empty cells
            if(neighbors[i] > 0)
            {
                if(neighbors[i] > cellValue)
                {
                    numberOfNumberOfOlderCells++;
                }
                else
                {
                    numberOfNumberOfYoungerCells++;
                }
            }
        }
        if(numberOfNumberOfYoungerCells < numberOfNumberOfOlderCells)
        {
            cellValue = 0;
        }
        return cellValue;
    }

    /**
     * Check to see if a cell will be born. A cell will be born if the the
     * cell's value is zero and there are two or more cells with the same value
     * the cell will be given a value of one more then the value of those cells.
     * The cells with the highest value will take precidence.
     * 
     * @param neighbors
     *            The cells in the neighborhood of this cell.
     * 
     * @return 0 if there is no birth or one more then the value fo the
     *         neighbors with the highest value where there are two or more in
     *         the neighborhood.
     */
    private int getBirths(int[] neighbors)
    {
        // We want to give birth to a cell that will be one
        // greater then the greatest cell value that has two
        // or more cells in the neighborhood.
        int greatestParents = 0;

        // We will only get in here if the cellValue is empty
        for(int i = 0; i < neighbors.length; i++)
        {
            if(neighbors[i] > greatestParents)
            {
                greatestParents = neighbors[i];
            }
        }

        // NOTE FROM DAVE: this check that there is another cell with the same
        // *OR* lower value
        for(int i = greatestParents; i > 0; i--)
        {
            boolean hasOne = false;
            for(int j = 0; j < neighbors.length; j++)
            {
                if(i == neighbors[j])
                {
                    if(hasOne)
                    {
                        return i + 1;
                    }
                    hasOne = true;
                }
            }
        }

        // We will only get here if there is no birth.
        return 0;
    }

    /**
     * A brief description (written in HTML) that describes what parameters will
     * give best results for this rule (which lattice, how many states, etc).
     * The description will be displayed on the properties panel. Using html
     * permits line breaks, font colors, etcetera, as described in HTML
     * resources. Regular line breaks will not work.
     * <p>
     * Recommend starting with the title of the rule followed by "For best
     * results, ...". See Rule 102 for an example.
     * 
     * @return An HTML string describing how to get best results from this rule.
     *         May be null.
     */
    public String getBestResultsDescription()
    {
        return BEST_RESULTS;
    }

    /**
     * Returns the name of this rule.
     * 
     * @return The name of this rule.
     */
    public String getDisplayName()
    {
        return RULE_NAME;
    }

    /**
     * Returns the tool tip for this rule.
     * 
     * @return The tool Tip for this rule.
     */
    public String getToolTipDescription()
    {
        return TOOLTIP;
    }

}
