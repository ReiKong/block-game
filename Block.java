package assignment3;

import java.util.ArrayList;
import java.util.Random;
import java.awt.Color;

public class Block {
    private int xCoord;
    private int yCoord;
    private int size; // height/width of the square
    private int level; // the root (outer most block) is at level 0
    private int maxDepth;
    private Color color;
    private Block[] children; // {UR, UL, LL, LR}

    public static Random gen = new Random();

    public Block() {}

    public Block(int x, int y, int size, int lvl, int  maxD, Color c, Block[] subBlocks) {
        this.xCoord = x;
        this.yCoord = y;
        this.size = size;
        this.level = lvl;
        this.maxDepth = maxD;
        this.color = c;
        this.children = subBlocks;
    }

    public Block(int lvl, int maxDepth) {
        this.level = lvl;
        this.maxDepth = maxDepth;
        boolean canBeSubdivided = this.level < this.maxDepth;

        if (lvl > maxDepth) {
            throw new IllegalArgumentException("Invalid input.");
        }

        if (canBeSubdivided) { // can be subdivided
            double doubRand = gen.nextDouble();
            double exponent = Math.exp(-0.25 * this.level);
            canBeSubdivided = doubRand < exponent;

            if (canBeSubdivided) {
                this.children = new Block[4];
                for (int i = 0; i < this.children.length; i++) {
                    this.children[i] = new Block(this.level + 1, this.maxDepth);
                }

                //this.children[0] = new Block(this.level + 1, this.maxDepth);
                //this.children[1] = new Block(this.level + 1, this.maxDepth);
                //this.children[2] = new Block(this.level + 1, this.maxDepth);
                //this.children[3] = new Block(this.level + 1, this.maxDepth);
            }
        }

        if (!canBeSubdivided) { // cannot be subdivided
            this.children = new Block[0]; // ?
            int randColor = gen.nextInt(4);
            this.color = GameColors.BLOCK_COLORS[randColor];
        }
    }

    public void updateSizeAndPosition(int size, int xCoord, int yCoord) {
        // int numSubdivisions = (int) Math.pow(2.0, maxDepth - 1);
        boolean evenlyDivided = (size % 2 == 0) || (size % 2 != 0 && this.level == this.maxDepth);
        boolean greaterThanZero = size > 0;
        if (!(greaterThanZero && evenlyDivided)) {
            throw new IllegalArgumentException("Invalid size and position input.");
        }

        this.size = size;
        this.xCoord = xCoord;
        this.yCoord = yCoord;

        if (this.children.length == 4) {
            int childrenSize = this.size / 2;
            this.children[0].updateSizeAndPosition(childrenSize, this.xCoord + childrenSize, this.yCoord);
            this.children[1].updateSizeAndPosition(childrenSize, this.xCoord, this.yCoord);
            this.children[2].updateSizeAndPosition(childrenSize, this.xCoord, this.yCoord + childrenSize);
            this.children[3].updateSizeAndPosition(childrenSize, this.xCoord + childrenSize, this.yCoord + childrenSize);
        }
    }


    /*
     * Returns a List of blocks to be drawn to get a graphical representation of this block.
     *
     * This includes, for each undivided Block:
     * - one BlockToDraw in the color of the block
     * - another one in the FRAME_COLOR and stroke thickness 3
     *
     * Note that a stroke thickness equal to 0 indicates that the block should be filled with its color.
     *
     * The order in which the blocks to draw appear in the list does NOT matter.
     */
    public ArrayList<BlockToDraw> getBlocksToDraw() {
        ArrayList<BlockToDraw> blocksToDraw = new ArrayList<>();

        if (this.children.length == 0) {
            BlockToDraw color = new BlockToDraw(this.color, this.xCoord, this.yCoord, this.size, 0);
            BlockToDraw frame = new BlockToDraw(GameColors.FRAME_COLOR, this.xCoord, this.yCoord, this.size, 3);
            blocksToDraw.add(color);
            blocksToDraw.add(frame);

        } else { // has smaller blocks
            for (Block child : this.children) {
                ArrayList<BlockToDraw> childBlocksToDraw = child.getBlocksToDraw();
                blocksToDraw.addAll(childBlocksToDraw);
            }
        }

        return blocksToDraw;
    }

    public BlockToDraw getHighlightedFrame() {
        return new BlockToDraw(GameColors.HIGHLIGHT_COLOR, this.xCoord, this.yCoord, this.size, 5);
    }

    /*
     * Return the Block within this Block that includes the given location
     * and is at the given level. If the level specified is lower than
     * the lowest block at the specified location, then return the block
     * at the location with the closest level value.
     *
     * The location is specified by its (x, y) coordinates. The lvl indicates
     * the level of the desired Block. Note that if a Block includes the location
     * (x, y), and that Block is subdivided, then one of its sub-Blocks will
     * contain the location (x, y) too. This is why we need lvl to identify
     * which Block should be returned.
     *
     * Input validation:
     * - this.level <= lvl <= maxDepth (if not throw exception)
     * - if (x,y) is not within this Block, return null.
     */
    public Block getSelectedBlock(int x, int y, int lvl) {
        if (!(this.level <= lvl && this.maxDepth >= lvl)) { // throws exception if !(this.level <= lvl <= maxDepth)
            throw new IllegalArgumentException("Invalid input.");
        }

        boolean withinX = this.xCoord <= x && this.xCoord + this.size >= x;
        boolean withinY = this.yCoord <= y && this.yCoord + this.size >= y;
        if (withinX && withinY && (this.level == lvl || this.children.length == 0)) {
            return this;
        }

        int childrenSize = this.size / 2;
        boolean withinRightX = this.xCoord + childrenSize <= x && this.xCoord + this.size >= x;
        boolean withinLeftX = this.xCoord <= x && this.xCoord + childrenSize >= x;
        boolean withinUpperY = this.yCoord <= y && this.yCoord + childrenSize >= y;
        boolean withinLowerY = this.yCoord + childrenSize <= y && this.yCoord + this.size >= y;
        if (withinRightX && withinUpperY) {                   // within bounds for UR child
            return this.children[0].getSelectedBlock(x, y, lvl);
        } else if (withinLeftX && withinUpperY) {            // within bounds for UL child
            return this.children[1].getSelectedBlock(x, y, lvl);
        } else if (withinLeftX && withinLowerY) {            // within bounds for LL child
            return this.children[2].getSelectedBlock(x, y, lvl);
        } else if (withinRightX && withinLowerY) {            // within bounds for LR child
            return this.children[3].getSelectedBlock(x, y, lvl);
        }

        return null;                                          // (!withinX && !withinY)
    }


    public int getUnitSize() {
        return this.size / (int) Math.pow(2.0, this.maxDepth);
    }


    /*
     * Swaps the child Blocks of this Block.
     * If input is 1, swap vertically. If 0, swap horizontally.
     * If this Block has no children, do nothing. The swap
     * should be propagate, effectively implementing a reflection
     * over the x-axis or over the y-axis.
     *
     */
    public void reflect(int direction) {
        if (!(direction == 1 || direction == 0)) {
            throw new IllegalArgumentException("Invalid input.");
        }

        if (this.children.length > 0) {
            Block URTemp = this.children[0];            // store UR
            Block LLTemp = this.children[2];            // store LL

            if (direction == 0) {                       // reflection over x-axis
                this.children[0] = this.children[3];    // set UR to LR
                this.children[3] = URTemp;              // set LR to UR
                this.children[2] = this.children[1];    // set LL to UL
                this.children[1] = LLTemp;              // set UL to LL

            } else {                                    // reflection over y-axis
                this.children[0] = this.children[1];    // set UR to UL
                this.children[1] = URTemp;              // set UL to UR
                this.children[2] = this.children[3];    // set LL to LR
                this.children[3] = LLTemp;              // set LR to LL
            }

            for (Block child : this.children) {
                child.reflect(direction);
            }

            this.updateSizeAndPosition(this.size, this.xCoord, this.yCoord);
        } // if no children, do nothing
    }


    /*
     * Rotate this Block and all its descendants.
     * If the input is 1, rotate clockwise. If 0, rotate
     * counterclockwise. If this Block has no children, do nothing.
     */
    public void rotate(int direction) {
        if (!(direction == 1 || direction == 0)) {
            throw new IllegalArgumentException("Invalid input.");
        }

        if (this.children.length > 0) {
            Block URTemp = this.children[0];            // store UR
            Block LLTemp = this.children[2];            // store LL

            if (direction == 1) {                       // counter-clockwise
                this.children[0] = this.children[1];    // set UR to UL
                this.children[1] = LLTemp;              // set UL to LL
                this.children[2] = this.children[3];    // set LL to UL
                this.children[3] = URTemp;              // set LR to UR

            } else {                                    // clockwise
                this.children[0] = this.children[3];    // set UR to LR
                this.children[3] = LLTemp;              // set LR to LL
                this.children[2] = this.children[1];    // set LL to UL
                this.children[1] = URTemp;              // set UL to UR
            }

            for (Block child : this.children) {
                child.rotate(direction);
            }

            this.updateSizeAndPosition(this.size, this.xCoord, this.yCoord);
        }
    }


    /*
     * Smash this Block.
     *
     * If this Block can be smashed,
     * randomly generate four new children Blocks for it.
     * (If it already had children Blocks, discard them.)
     * Ensure that the invariants of the Blocks remain satisfied.
     *
     * A Block can be smashed iff it is not the top-level Block
     * and it is not already at the level of the maximum depth.
     *
     * Return True if this Block was smashed and False otherwise.
     *
     */
    public boolean smash() {
        if (this.level != 0 && this.level != this.maxDepth) {
            this.children = new Block[4];

            for (int i = 0; i < this.children.length; i++) {
                this.children[i] = new Block(this.level + 1, this.maxDepth);
            }

            this.updateSizeAndPosition(this.size, this.xCoord, this.yCoord);

            return true;
        }
        return false;
    }


    /*
     * Return a two-dimensional array representing this Block as rows and columns of unit cells.
     *
     * Return and array arr where, arr[i] represents the unit cells in row i,
     * arr[i][j] is the color of unit cell in row i and column j.
     *
     * arr[0][0] is the color of the unit cell in the upper left corner of this Block.
     */
    public void flattenHelper(Color[][] colorArray, int i, int j, int unitSize) {
        if (this.children.length == 0) {
            // figure out what unit cells the block covers
            int endI = i + this.size / unitSize;
            int endJ = j + this.size / unitSize;


            // add to array
            for (int x = i; x < endI; x++) {
                for (int y = j; y < endJ; y++) {
                    colorArray[x][y] = this.color;
                }

            }
        } else {
            for (int k = 0; k < this.children.length; k++) {
                int childI;
                int childJ;

                if (k == 0) {
                    childI = i;
                    childJ = j + this.size / 2 / unitSize;
                } else if (k == 1) {
                    childI = i;
                    childJ = j;
                } else if (k == 2) {
                    childI = i + this.size / 2 / unitSize;
                    childJ = j;
                } else { // k == 3
                    childI = i + this.size / 2 / unitSize;
                    childJ = j + this.size / 2 / unitSize;
                }

                this.children[k].flattenHelper(colorArray, childI, childJ, unitSize);
            }
        }
    }

    public Color[][] flatten() {
        int unitSize = this.getUnitSize();
        int numUnitsOnSide = this.size / unitSize;
        Color[][] colorArray = new Color[numUnitsOnSide][numUnitsOnSide];

        this.flattenHelper(colorArray, 0, 0, unitSize);

        return colorArray;
    }



    // These two get methods have been provided. Do NOT modify them.
    public int getMaxDepth() {
        return this.maxDepth;
    }

    public int getLevel() {
        return this.level;
    }


    /*
     * The next 5 methods are needed to get a text representation of a block.
     * You can use them for debugging. You can modify these methods if you wish.
     */
    public String toString() {
        return String.format("pos=(%d,%d), size=%d, level=%d"
                , this.xCoord, this.yCoord, this.size, this.level);
    }

    public void printBlock() {
        this.printBlockIndented(0);
    }

    private void printBlockIndented(int indentation) {
        String indent = "";
        for (int i=0; i<indentation; i++) {
            indent += "\t";
        }

        if (this.children.length == 0) {
            // it's a leaf. Print the color!
            String colorInfo = GameColors.colorToString(this.color) + ", ";
            System.out.println(indent + colorInfo + this);
        } else {
            System.out.println(indent + this);
            for (Block b : this.children)
                b.printBlockIndented(indentation + 1);
        }
    }

    private static void coloredPrint(String message, Color color) {
        System.out.print(GameColors.colorToANSIColor(color));
        System.out.print(message);
        System.out.print(GameColors.colorToANSIColor(Color.WHITE));
    }

    public void printColoredBlock(){
        Color[][] colorArray = this.flatten();
        for (Color[] colors : colorArray) {
            for (Color value : colors) {
                String colorName = GameColors.colorToString(value).toUpperCase();
                if(colorName.length() == 0){
                    colorName = "\u2588";
                }else{
                    colorName = colorName.substring(0, 1);
                }
                coloredPrint(colorName, value);
            }
            System.out.println();
        }
    }

}
