package assignment3;

import java.awt.Color;

public class BlobGoal extends Goal{

	public BlobGoal(Color c) {
		super(c);
	}

	@Override
	public int score(Block board) {
		Color[][] unitCells = board.flatten();
		int score = 0;
		int maxScore = 0;
		int sideLength = unitCells.length;
		boolean[][] visited = new boolean[sideLength][sideLength];

		for (int i = 0; i < sideLength; i++) {
			for (int j = 0; j < sideLength; j++) {
				if (!visited[i][j] && unitCells[i][j].equals(targetGoal)) {
					score = undiscoveredBlobSize(i, j, unitCells, visited);
					if (score > maxScore) {
						maxScore = score;
					}
				}
			}
		}
		return maxScore;
	}


	@Override
	public String description() {
		return "Create the largest connected blob of " + GameColors.colorToString(targetGoal) 
		+ " blocks, anywhere within the block";
	}


	public int undiscoveredBlobSize(int i, int j, Color[][] unitCells, boolean[][] visited) {
		if (unitCells[i][j].equals(targetGoal) && !visited[i][j]) {
			visited[i][j] = true;
			int left = 0, right = 0, down = 0, up = 0;

			if (i - 1 >= 0) {
				up = undiscoveredBlobSize(i - 1, j, unitCells, visited);
			}

			if (i + 1 <= unitCells.length - 1) {
				down = undiscoveredBlobSize(i + 1, j, unitCells, visited);
			}

			if (j + 1 <= unitCells.length - 1) {
				right = undiscoveredBlobSize(i, j + 1, unitCells, visited);
			}

			if (j - 1 >= 0) {
				left = undiscoveredBlobSize(i, j - 1, unitCells, visited);
			}

			return 1 + left + right + down + up;
		} else {
			return 0;
		}
	}

}
