/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright © 2015 51Degrees Mobile Experts Limited, 5 Charlotte Close,
 * Caversham, Reading, Berkshire, United Kingdom RG4 7BY
 * 
 * This Source Code Form is the subject of the following patent 
 * applications, owned by 51Degrees Mobile Experts Limited of 5 Charlotte
 * Close, Caversham, Reading, Berkshire, United Kingdom RG4 7BY: 
 * European Patent Application No. 13192291.6; and 
 * United States Patent Application Nos. 14/085,223 and 14/085,301.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0.
 * 
 * If a copy of the MPL was not distributed with this file, You can obtain
 * one at http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */
package fiftyone.mobile.detection.entities;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.MatchState;
import fiftyone.mobile.detection.readers.BinaryReader;
import fiftyone.mobile.detection.search.SearchArrays;
import java.io.IOException;
import java.util.List;

/**
 * A node in the tree of characters for each character position.
 * <p>
 * Every character position in the string contains a tree of nodes
 * which are evaluated until either a complete node is found, or 
 * no nodes are found that match at the character position.
 * <p>
 * The list of Signature entities is in ascending order of 
 * the complete nodes which form the sub strings of the signature.
 * Complete nodes are found at detection time for the target User-Agent
 * and then used to search for a corresponding signature. If one does
 * not exist then Signatures associated with the nodes that were found 
 * are evaluated to find one that is closest to the target User-Agent.
 * <p>
 * Root nodes are the first node at a character position. It's children
 * are based on sequences of characters that if present lead to the 
 * next node. A complete node will represent a sub string within
 * the User-Agent.
 * <p>
 * For more information see https://51degrees.com/Support/Documentation/Java
 */
public abstract class Node extends BaseEntity implements Comparable<Node> {
    
    /**
     * The length of a node index.
     */
    public static final int NODE_INDEX_LENGTH = 9;
    
    /**
     * The length of a numeric node index.
     */
    public static final int NODE_NUMERIC_INDEX_LENGTH = 6;
    
    /**
     * Used to find matching numeric nodes.
     */
    private static final SearchNodeNumericIndex numericChildrenSearch = 
            new SearchNodeNumericIndex();
    
    /**
     * Ranges of numeric values for comparison.
     */
    private static final Range[] ranges = new Range[]{
        new Range((short) 0, (short) 10),
        new Range((short) 10, (short) 100),
        new Range((short) 100, (short) 1000),
        new Range((short) 1000, (short) 10000),
        new Range((short) 10000, Short.MAX_VALUE)
    };
    
    /**
     * The characters that make up the node if it's a complete node or null 
     * if it's incomplete.
     */
    @SuppressWarnings("VolatileArrayField")
    private volatile byte[] characters;
    
    /**
     * The position in the stream of the first byte of the node.
     */
    protected final long nodeStartStreamPosition;
    
    /**
     * Number of numeric children associated with the node.
     */
    protected final short numericChildrenCount;
    
    /**
     * Number of children associated with the node.
     */
    protected final short childrenCount;
    
    /**
     * A list of all the child node indexes.
     */
    protected NodeIndex[] children;
    
    /**
     * The parent index for this node.
     */
    final int parentOffset;
    
    /**
     * The offset in the strings data structure to the string that contains all
     * the characters of the node. Or -1 if the node is not complete and no
     * characters are available.
     */
    private final int characterStringOffset;
    /**
     * The next character position to the left of this node or a negative number
     * if this is not a complete node.
     */
    public final short nextCharacterPosition;
    /**
     * The position of the first character the node represents in the signature
     * or target User-Agent.
     */
    public final short position;
    /**
     * Number of ranked signature indexes associated with the node.
     */
    protected int rankedSignatureCount;
    /**
     * Parent node for this node.
     */
    private volatile Node parent = null;
    /**
     * Root node for this node.
     */
    private volatile Node root;

    /**
     * Constructs a new instance of Node
     *
     * @param dataSet The data set the node is contained within.
     * @param offset The offset in the data structure to the node.
     * @param reader BinaryReader object to be used.
     */
    public Node(Dataset dataSet, int offset, BinaryReader reader) {
        super(dataSet, offset);
        this.nodeStartStreamPosition = reader.getPos(); 
        this.position = reader.readInt16();
        this.nextCharacterPosition = reader.readInt16();
        this.parentOffset = reader.readInt32();
        this.characterStringOffset = reader.readInt32();
        this.childrenCount = reader.readInt16();
        this.numericChildrenCount = reader.readInt16();
    }
    
    /**
     * Returns the root node for this node.
     * 
     * @return root node for this node
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public Node getRoot() throws IOException {
        Node localRoot = root;
        if (localRoot == null) {
            synchronized (this) {
                localRoot = root;
                if (localRoot == null) {
                    root = localRoot = getParent() == null ? this : getParent().getRoot();
                }
            }
        }
        return localRoot;
    }

    /**
     * Returns the parent node for this node.
     * @reurn the parent node for this node.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    Node getParent() throws IOException {
        Node localParent = parent;
        if (parentOffset >= 0 && localParent == null) {
            synchronized (this) {
                localParent = parent;
                if (localParent == null) {
                    parent = localParent = getDataSet().getNodes().get(parentOffset);
                }
            }
        }
        return localParent;
    }

    /**
     * Returns true if this node represents a completed sub string and the next
     * character position is set.
     */
    boolean isComplete() {
        return characterStringOffset >= 0;
    }

    /**
     * Compares one node to another for the purposes of determining the
     * signature the node relates to.
     *
     * @param other node to be compared.
     * @return -1 if this node is lower than the other, 1 if higher or 0 if
     * equal.
     */
    @Override
    public int compareTo(Node other) {
        return super.compareTo(other);
    }    
    
    /**
     * Returns the number of characters in the node tree.
     * 
     * @return number of characters in the node tree
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public int getLength() throws IOException {
        return getRoot().position - position;
    }

    /**
     * Gets an array containing all the characters of the node.
     * 
     * @return array containing all the characters of the node.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    @SuppressWarnings("DoubleCheckedLocking")
    public byte[] getCharacters() throws IOException {
        byte[] localCharacters = characters;
        if (localCharacters == null && characterStringOffset >= 0) {
            synchronized (this) {
                localCharacters = characters;
                if (localCharacters == null) {
                    characters = localCharacters = super.getDataSet().strings.
                            get(characterStringOffset).value;
                }
            }
        }
        return localCharacters;
    }

    /**
     * Used by the constructor to read the variable length list of child
     * indexes that contain numeric values.
     * 
     * @param dataSet The data set the node is contained within.
     * @param reader Reader connected to the source data structure and 
     * positioned to start reading.
     * @param count The number of node indexes that need to be read.
     * @return variable length list of child indexes that contain numeric 
     * values.
     */
    public NodeNumericIndex[] readNodeNumericIndexes(Dataset dataSet, 
                                            BinaryReader reader, short count) {
        if (count <= 0) {
            return new NodeNumericIndex[0];
        }
        NodeNumericIndex[] array = new NodeNumericIndex[count];
        for (int i = 0; i < array.length; i++) {
            array[i] = new NodeNumericIndex(dataSet, 
                                    reader.readInt16(), reader.readInt32());
        }
        return array;
    }
    
    /**
     * @return number of elements in the children array.
     */
    public int getChildrenLength() {
        return children.length;
    }

    /**
     * @return number of element in the numericChildren array.
     */
    public int getNumericChildrenLength() {
        return numericChildrenCount;
    }

    /**
     * Called after the entire data set has been loaded to ensure any further
     * initialisation steps that require other items in the data set can be
     * completed.
     * <p>
     * This method should not be called as it is part of the internal logic.
     * 
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public void init() throws IOException {
        if (isComplete() && characters == null) {
            characters = getCharacters();
        }
        if (parent == null && parentOffset >= 0) {
            parent = dataSet.nodes.get(parentOffset);
        }
        if (root == null) {
            root = parent == null ? this : parent.root;
        }
        for (NodeIndex child : children) {
            child.init();
        }
    }
    
    /**
     * Gets a complete node, or if one isn't available exactly the closest 
     * numeric one to the target User-Agent at the current position.
     * 
     * @param state current working state of the matching process
     * @return a complete node, or if one isn't available exactly the closest 
     * numeric one.
     * @throws IOException if there was a problem accessing data file.
     */
    public Node getCompleteNumericNode(MatchState state) throws IOException {
        Node node = null;

        // Check to see if there's a next node which matches
        // exactly.
        Node nextNode = getNextNode(state);
        if (nextNode != null) {
            node = nextNode.getCompleteNumericNode(state);
        }

        if (node == null && numericChildrenCount > 0) {
            // No. So try each of the numeric matches in ascending order of
            // difference.
            int target = getCurrentPositionAsNumeric(state);
            if (target >= 0) {
                NodeNumericIndexIterator iterator = 
                        getNumericNodeIterator(target);
                if (iterator != null) {
                    while (iterator.hasNext()) {
                        NodeNumericIndex current = iterator.next();
                        node = current.getNode().getCompleteNumericNode(state);
                        if (node != null) {
                            int difference = 
                                    Math.abs(target - current.getValue());
                            state.incrLowestScore(difference);
                            break;
                        }
                    }
                }
            }
        }
        if (node == null && isComplete()) {
            node = this;
        }
        return node;
    }

    /**
     * Returns a complete node for the match object provided.
     *
     * @param state current working state of the matching process.
     * @return The next child node, or null if there isn't one.
     * @throws IOException if there was a problem accessing data file.
     */
    public Node getCompleteNode(MatchState state) throws IOException {
        Node node = null;
        Node nextNode = getNextNode(state);
        if (nextNode != null) {
            node = nextNode.getCompleteNode(state);
        }
        if (node == null && isComplete()) {
            node = this;
        }
        return node;
    }

    /**
     * Returns true if any of the nodes in the match have overlapping characters
     * with this one.
     *
     * @param state current working state of the matching process
     * @return true if any of the nodes in the match have overlapping characters
     * with this one.
     * @throws IOException if there was a problem accessing data file.
     */
    public boolean getIsOverlap(MatchState state) throws IOException {
        for (Node node : state.getNodesList()) {
            if (getIsOverlap(node)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the characters for this node to the values array.
     *
     * @param values array to add characters to.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public void addCharacters(byte[] values) throws IOException {
        if (getParent() != null) {
            byte[] nodeCharacters = this.characters == null ?
                    this.dataSet.strings.get(this.characterStringOffset).value :
                    this.getCharacters();
            for (int i = 0; i < getLength(); i++) {
                values[position + i + 1] = nodeCharacters[i];
            }
        }
    }

    /**
     * Returns a string of spaces with the characters relating to this node
     * populated.
     *
     * @return a string representation of the node
     */
    @Override
    public String toString() {
        try {
            byte[] values = new byte[getDataSet().maxUserAgentLength];
            addCharacters(values);
            for (int i = 0; i < values.length; i++) {
                if (values[i] == 0) {
                    values[i] = (byte) ' ';
                }
            }
            return new String(values);
        } catch (IOException e) {
            return super.toString();
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Private methods">
    /**
     * Returns the node position as a number.
     *
     * @param state current working state of the matching process
     * @return Null if there is no numeric characters, otherwise the characters
     * as an integer
     */
    private int getCurrentPositionAsNumeric(MatchState state) {
        // Find the left most numeric character from the current position.
        int i = position;
        while (i >= 0
                && state.getTargetUserAgentArray()[i] >= (byte) '0'
                && state.getTargetUserAgentArray()[i] <= (byte) '9') {
            i--;
        }
        if (i < position) {
            return getNumber(
                    state.getTargetUserAgentArray(),
                    i + 1,
                    position - i);
        }
        return -1;
    }
    
    /**
     * Returns true if the node overlaps with this one.
     *
     * @param node the other node.
     * @return true if the node overlaps with this one.
     * @throws IOException if there was a problem accessing data file.
     */
    private boolean getIsOverlap(Node node) throws IOException {
        Node lower = node.position < position ? node : this;
        Node higher = lower == this ? node : this;
        return lower.position == higher.position
                || lower.getRoot().position > higher.position;
    }
    
    /**
     * Returns the next node for the characters provided from the start index
     * for the number of characters specified.
     *
     * @param state current working state of the matching process
     * @return The next child node, or null if there isn't one
     */
    private Node getNextNode(MatchState state) throws IOException {
        int upper = children.length - 1;

        if (upper >= 0) {

            int lower = 0;
            int middle = lower + (upper - lower) / 2;
            int length = children[middle].getCharacters().length;
            int startIndex = position - length + 1;

            while (lower <= upper) {
                middle = lower + (upper - lower) / 2;

                // Increase the number of strings checked.
                if (children[middle].isString) {
                    state.incrStringsRead();
                }

                // Increase the number of nodes checked.
                state.incrNodesEvaluated();

                int comparisonResult = children[middle].compareTo(
                        state.getTargetUserAgentArray(), startIndex);
                if (comparisonResult == 0) {
                    return children[middle].getNode();
                } else if (comparisonResult > 0) {
                    upper = middle - 1;
                } else {
                    lower = middle + 1;
                }
            }
        }

        return null;
    }
    
    /**
     * Provides an iterator which provides the closest numeric children to the
     * target is ascending order of difference
     *
     * @param target value of the sub string in the User-Agent
     * @return an iterator configured to provide numeric children in ascending
     * order of difference
     */
    private NodeNumericIndexIterator getNumericNodeIterator(int target) 
            throws IOException {

        if (target >= 0 && target <= Short.MAX_VALUE) {
            Range range = getRange(target);

            int startIndex = numericChildrenSearch.binarySearch(
                    getNumericChildren(), target);
            if (startIndex < 0) {
                startIndex = ~startIndex - 1;
            }

            return new NodeNumericIndexIterator(
                    range, getNumericChildren(), target, startIndex);
        }

        return null;
    }
    
    /**
     * Determines the range the target value falls between
     *
     * @param target value whose range is required
     * @return the range the target falls between
     */
    private Range getRange(int target) {
        for (Range range : ranges) {
            if (range.inRange(target)) {
                return range;
            }
        }
        return ranges[ranges.length - 1];
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Abstract methods.">
    /**
     * @return an array of all the numeric children.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public abstract NodeNumericIndex[] getNumericChildren() throws IOException;
    
    /**
     * @return An array of the ranked signature indexes for the node.
     * @throws java.io.IOException if there was a problem accessing data file.
     */
    public abstract List<Integer> getRankedSignatureIndexes() throws IOException;
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Iterator for NodeNumericIndex">
    static class NodeNumericIndexIterator {

        private final NodeNumericIndex[] array;
        private final int target;
        private final Range range;
        private int lowIndex;
        private int highIndex;
        private boolean lowInRange;
        private boolean highInRange;

        /**
         * Creates a new NideNumericIndexIterator.
         * 
         * @param range the range of values the iterator can return
         * @param array array of items that could be returned
         * @param target the target value
         * @param startIndex start index in the array
         */
        NodeNumericIndexIterator(Range range, NodeNumericIndex[] array,
                                 int target, int startIndex) {
            this.range = range;
            this.array = array;
            this.target = target;

            lowIndex = startIndex;
            highIndex = startIndex + 1;

            // Determine if the low and high indexes are in range.
            lowInRange = lowIndex >= 0 && lowIndex < array.length
                    && this.range.inRange(array[lowIndex].getValue());
            highInRange = highIndex < array.length && highIndex >= 0
                    && this.range.inRange(array[highIndex].getValue());
        }

        boolean hasNext() {
            return lowInRange || highInRange;
        }

        NodeNumericIndex next() {
            int index;

            if (lowInRange && highInRange) {
                // Get the differences between the two values.
                int lowDifference = 
                        Math.abs(array[lowIndex].getValue() - target);
                int highDifference = 
                        Math.abs(array[highIndex].getValue() - target);

                // Favour the lowest value where the differences are equal.
                if (lowDifference <= highDifference) {
                    index = lowIndex;

                    // Move to the next low index.
                    lowIndex--;
                    lowInRange = lowIndex >= 0
                            && range.inRange(array[lowIndex].getValue());
                } else {
                    index = highIndex;

                    // Move to the next high index.
                    highIndex++;
                    highInRange = highIndex < array.length
                            && range.inRange(array[highIndex].getValue());
                }
            } else if (lowInRange) {
                index = lowIndex;

                // Move to the next low index.
                lowIndex--;
                lowInRange = lowIndex >= 0
                        && range.inRange(array[lowIndex].getValue());
            } else {
                index = highIndex;

                // Move to the next high index.
                highIndex++;
                highInRange = highIndex < array.length
                        && range.inRange(array[highIndex].getValue());
            }

            if (index >= 0) {
                return array[index];
            }
            return null;
        }
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Private static class for binary search access">
    /**
     * Class provides access to the binary search functionality and overrides 
     * the compareTo method. 
     */
    private static class SearchNodeNumericIndex 
                            extends SearchArrays<NodeNumericIndex, Integer> {
        
        @Override
        public int compareTo(NodeNumericIndex item, Integer key) {
            return item.compareTo(key);
        }
    }
    // </editor-fold>
}
