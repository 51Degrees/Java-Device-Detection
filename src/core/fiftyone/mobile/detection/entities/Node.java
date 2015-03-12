package fiftyone.mobile.detection.entities;

import fiftyone.mobile.detection.Dataset;
import fiftyone.mobile.detection.Match;
import fiftyone.mobile.detection.readers.BinaryReader;
import java.io.IOException;

/* *********************************************************************
 * This Source Code Form is copyright of 51Degrees Mobile Experts Limited. 
 * Copyright 2014 51Degrees Mobile Experts Limited, 5 Charlotte Close,
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
/**
 * A node in the tree of characters for each character position. <p> Every
 * character position in the string contains a tree of nodes which are evaluated
 * until either a complete node is found, or no nodes are found that match at
 * the character position. <p> The list of Signature entities is in ascending
 * order of the complete nodes which form the sub strings of the signature.
 * Complete nodes are found at detection time for the target user agent and then
 * used to search for a corresponding signature. If one does not exist then
 * Signatures associated with the nodes that were found are evaluated to find
 * one that is closest to the target user agent. <p> Root nodes are the first
 * node at a character position. It's children are based on sequences of
 * characters that if present lead to the next node. A complete node will
 * represent a sub string within the user agent.
 */
/**
 * A node in the trie data structures held at each character position.
 */
public class Node extends BaseEntity implements Comparable<Node> {

    class NodeNumericIndexIterator {

        private final NodeNumericIndex[] array;
        private final int target;
        private final Range range;
        private int lowIndex;
        private int highIndex;
        private boolean lowInRange;
        private boolean highInRange;

        /**
         *
         * @param range the range of values the iterator can return
         * @param array array of items that could be returned
         * @param target the target value
         * @param startIndex start index in the array
         */
        NodeNumericIndexIterator(
                Range range, NodeNumericIndex[] array,
                int target, int startIndex) {
            this.range = range;
            this.array = array;
            this.target = target;

            lowIndex = startIndex;
            highIndex = startIndex + 1;

            // Determine if the low and high indexes are in range.
            lowInRange = lowIndex >= 0 && lowIndex < array.length
                    && range.inRange(array[lowIndex].getValue());
            highInRange = highIndex < array.length && highIndex >= 0
                    && range.inRange(array[highIndex].getValue());
        }

        boolean hasNext() {
            return lowInRange || highInRange;
        }

        NodeNumericIndex next() {
            int index = -1;

            if (lowInRange && highInRange) {
                // Get the differences between the two values.
                int lowDifference = Math.abs(array[lowIndex].getValue() - target);
                int highDifference = Math.abs(array[highIndex].getValue() - target);

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
    /**
     * The length of a node index.
     */
    public static final int NODE_INDEX_LENGTH = 9;
    /**
     * The length of a numeric node index.
     */
    public static final int NODE_NUMERIC_INDEX_LENGTH = 6;
    /**
     * The minimum length of a node assuming no node indexes or signatures.
     */
    public static final int MIN_LENGTH = 20;
    private static final Range[] ranges = new Range[]{
        new Range((short) 0, (short) 9),
        new Range((short) 10, (short) 99),
        new Range((short) 100, (short) 999),
        new Range((short) 1000, Short.MAX_VALUE)
    };

    /**
     * A list of all the signature indexes that relate to this node.
     */
    private final int[] signatureIndexes;
    /**
     * A list of all the child node indexes.
     */
    private final NodeIndex[] children;
    /**
     * An array of all the numeric children.
     */
    private final NodeNumericIndex[] numericChildren;
    /**
     * The parent index for this node.
     */
    final int parentIndex;
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
     * or target user agent.
     */
    public final short position;

    /**
     * Returns the root node for this node.
     * @return root node for this node
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public Node getRoot() throws IOException {
        if (root == null) {
            synchronized (this) {
                if (root == null) {
                    root = getParent() == null ? this : getParent().getRoot();
                }
            }
        }
        return root;
    }
    private Node root;

    /**
     * Returns the parent node for this node.
     */
    Node getParent() throws IOException {
        if (parentIndex >= 0 && parent == null) {
            synchronized (this) {
                if (parent == null) {
                    parent = getDataSet().getNodes().get(parentIndex);
                }
            }
        }
        return parent;
    }
    private Node parent = null;

    /**
     * Returns true if this node represents a completed sub string and the next
     * character position is set.
     */
    boolean isComplete() {
        return nextCharacterPosition != Short.MIN_VALUE;
    }

    /**
     * Returns the number of characters in the node tree.
     * @return number of characters in the node tree
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public int getLength() throws IOException {
        return getRoot().position - position;
    }

    /**
     * Gets an array containing all the characters of the node.
     * @return array containing all the characters of the node
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public byte[] getCharacters() throws IOException {
        if (characters == null && characterStringOffset >= 0) {
            synchronized (this) {
                if (characters == null) {
                    characters = super.getDataSet().strings.get(characterStringOffset).value;
                }
            }
        }
        return characters;
    }
    private byte[] characters = null;

    public int[] getRankedSignatureIndexes() {
        return signatureIndexes;
    }

    public int getChildrenLength() {
        return children.length;
    }

    public int getNumericChildrenLength() {
        return numericChildren.length;
    }

    /**
     * Constructs a new instance of Node
     *
     * @param dataSet The data set the node is contained within
     * @param offset The offset in the data structure to the node
     * @param reader BinaryReader object to be used
     */
    public Node(Dataset dataSet, int offset, BinaryReader reader) {
        super(dataSet, offset);
        this.position = reader.readInt16();
        this.nextCharacterPosition = reader.readInt16();
        this.parentIndex = reader.readInt32();
        this.characterStringOffset = reader.readInt32();
        short childrenCount = reader.readInt16();
        short numericChildrenCount = reader.readInt16();
        int signatureCount = reader.readInt32();
        this.children = readNodeIndexes(dataSet, reader, offset + MIN_LENGTH, childrenCount);
        this.numericChildren = readNodeNumericIndexes(dataSet, reader, numericChildrenCount);
        this.signatureIndexes = BaseEntity.readIntegerArray(reader, signatureCount);
    }

    /**
     * Used by the constructor to read the variable length list of child node
     * numeric indexes associated with the node.
     *
     * @param dataSet The data set the node is contained within
     * @param reader Reader connected to the source data structure and
     * positioned to start reading
     * @param count The number of elements to read into the array
     * @return An array of child numeric node indexes for the node
     */
    private static NodeNumericIndex[] readNodeNumericIndexes(Dataset dataSet,
            BinaryReader reader, short count) {
        NodeNumericIndex[] array = new NodeNumericIndex[count];
        for (int i = 0; i < array.length; i++) {
            array[i] = new NodeNumericIndex(dataSet, reader.readInt16(), reader.readInt32());
        }
        return array;
    }

    /**
     * Used by the constructor to read the variable length list of child node
     * indexes associated with the node.
     *
     * @param dataSet The data set the node is contained within
     * @param reader Reader connected to the source data structure and
     * positioned to start reading
     * @param offset The offset in the data structure to the node
     * @param count The number of elements to read into the array
     * @return An array of child node indexes for the node
     */
    private static NodeIndex[] readNodeIndexes(Dataset dataSet,
            BinaryReader reader, int offset, short count) {
        NodeIndex[] array = new NodeIndex[count];
        offset += 2;
        for (int i = 0; i < array.length; i++) {
            array[i] = new NodeIndex(dataSet, offset, reader.readBoolean(),
                    reader.readBytes(4), reader.readInt32());
            offset += NODE_INDEX_LENGTH;
        }
        return array;
    }

    /**
     * Called after the entire data set has been loaded to ensure any further
     * initialisation steps that require other items in the data set can be
     * completed.
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public void init() throws IOException {
        if (parentIndex >= 0) {
            parent = getDataSet().getNodes().get(parentIndex);
        }
        root = getParent() == null ? this : getParent().getRoot();
        for (NodeIndex child : children) {
            child.init();
        }
        getCharacters();
    }

    public Node getCompleteNumericNode(Match match) throws IOException {
        Node node = null;

        // Check to see if there's a next node which matches
        // exactly.
        Node nextNode = getNextNode(match);
        if (nextNode != null) {
            node = nextNode.getCompleteNumericNode(match);
        }

        if (node == null && numericChildren.length > 0) {
            // No. So try each of the numeric matches in ascending order of
            // difference.
            Integer target = getCurrentPositionAsNumeric(match);
            if (target != null) {
                NodeNumericIndexIterator iterator = getNumericNodeIterator(target);
                if (iterator != null) {
                    while (iterator.hasNext()) {
                        NodeNumericIndex current = iterator.next();
                        node = current.getNode().getCompleteNumericNode(match);
                        if (node != null) {
                            int difference = Math.abs(target - current.getValue());
                            if (match.getLowestScore() == null) {
                                match.setLowestScore(difference);
                            } else {
                                match.setLowestScore(match.getLowestScore() + difference);
                            }
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
     * Provides an iterator which provides the closest numeric children to the
     * target is ascending order of difference
     *
     * @param target value of the sub string in the user agent
     * @return an iterator configured to provide numeric children in ascending
     * order of difference
     */
    private NodeNumericIndexIterator getNumericNodeIterator(int target) {

        if (target >= 0 && target <= Short.MAX_VALUE) {
            Range range = getRange(target);

            int startIndex = super.binarySearch(numericChildren, target);
            if (startIndex < 0) {
                startIndex = ~startIndex - 1;
            }

            return new NodeNumericIndexIterator(range, numericChildren, target, startIndex);
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
        throw new IllegalArgumentException("target");
    }

    /**
     * Returns the node position as a number.
     *
     * @param match results including the target user agent
     * @return Null if there is no numeric characters, otherwise the characters
     * as an integer
     */
    private Integer getCurrentPositionAsNumeric(Match match) {
        int i = position;
        while (i >= 0
                && match.getTargetUserAgentArray()[i] >= (byte) '0'
                && match.getTargetUserAgentArray()[i] <= (byte) '9') {
            i--;
        }
        if (i < position) {
            return getNumber(
                    match.getTargetUserAgentArray(),
                    i + 1,
                    position - i);
        }
        return null;
    }

    /**
     * Returns an integer representation of the characters between start and
     * end. Assumes that all the characters are numeric characters.
     *
     * @param array Array of characters with numeric characters present between
     * start and end
     * @param start The first character to use to convert to a number
     * @param length The number of characters to use in the conversion
     * @return
     */
    private Integer getNumber(byte[] array, int start, int length) {
        int value = 0;
        for (int i = start + length - 1, p = 0; i >= start; i--, p++) {
            value += (int) (Math.pow(10, p)) * ((byte) array[i] - (byte) '0');
        }
        return value;
    }

    /**
     * Returns a complete node for the match object provided.
     *
     * @param match results including the target user agent
     * @return The next child node, or null if there isn't one
     * @throws IOException indicates an I/O exception occurred
     */
    public Node getCompleteNode(Match match) throws IOException {
        Node node = null;
        Node nextNode = getNextNode(match);
        if (nextNode != null) {
            node = nextNode.getCompleteNode(match);
        }
        if (node == null && isComplete()) {
            node = this;
        }
        return node;
    }

    /**
     * Returns the next node for the characters provided from the start index
     * for the number of characters specified.
     *
     * @param match Match results including the target user agent
     * @return The next child node, or null if there isn't one
     */
    Node getNextNode(Match match) throws IOException {
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
                    match.incrStringsRead();
                }

                // Increase the number of nodes checked.
                match.incrNodesEvaluated();

                int comparisonResult = children[middle].compareTo(
                        match.getTargetUserAgentArray(), startIndex);
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
     * Returns true if the node overlaps with this one.
     *
     * @param node
     * @return
     * @throws IOException
     */
    private boolean getIsOverlap(Node node) throws IOException {
        Node lower = node.position < position ? node : this;
        Node higher = lower == this ? node : this;
        return lower.position == higher.position
                || lower.getRoot().position > higher.position;
    }

    /**
     * Returns true if any of the nodes in the match have overlapping characters
     * with this one.
     *
     * @param match Match object to be checked for overlaps
     * @return true if any of the nodes in the match have overlapping characters
     * with this one.
     * @throws IOException indicates an I/O exception occurred
     */
    public boolean getIsOverlap(Match match) throws IOException {
        for (Node node : match.getNodes()) {
            if (getIsOverlap(node)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the characters for this node to the values array.
     *
     * @param values array to add characters to
     * @throws java.io.IOException indicates an I/O exception occurred
     */
    public void addCharacters(byte[] values) throws IOException {
        if (getParent() != null) {
            byte[] characters = null;
            for (NodeIndex child : getParent().children) {
                if (child.relatedNodeOffset == getIndex()) {
                    characters = child.getCharacters();
                    break;
                }
            }

            for (int i = 0; i < characters.length; i++) {
                values[position + i + 1] = characters[i];
            }

            getParent().addCharacters(values);
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

    /**
     * Compares one node to another for the purposes of determining the
     * signature the node relates to.
     *
     * @param other node to be compared
     * @return -1 if this node is lower than the other, 1 if higher or 0 if
     * equal.
     */
    public int compareTo(Node other) {
        return getIndex() - other.getIndex();
    }
}
