package assignment3;

import java.awt.Color;

public class PerimeterGoal extends Goal{

	public PerimeterGoal(Color c) {
		super(c);
	}

	@Override
	public int score(Block board) {
		Color[][] unitCells = board.flatten();
		int score = 0;
		int sideLength = unitCells.length;

		for (int i = 0; i < sideLength; i = i + sideLength - 1) { 			// top and bottom side lengths
			for (int j = 0; j < sideLength; j++) {
				if (unitCells[i][j] == targetGoal) {
					score++;
					//if (j == 0 || j == sideLength - 1) {
						//score++;
					//}
				}
			}
		}

		for (int i = 0; i < sideLength; i++) {								// left and right side lengths
			for (int j = 0; j < sideLength; j = j + sideLength - 1) {
				if (unitCells[i][j] == targetGoal) {
					score++;
					//if (j == 0 || j == sideLength - 1) {
						//score++;
					//}
				}
			}
		}

		return score;
	}

	@Override
	public String description() {
		return "Place the highest number of " + GameColors.colorToString(targetGoal) 
		+ " unit cells along the outer perimeter of the board. Corner cell count twice toward the final score!";
	}

}
