package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.Random;

public class MyGdxGame extends ApplicationAdapter {
	private float planetScale = 0.5f; // Adjust the scale factor as needed
	private int planetRange = 1280;
	SpriteBatch batch;
	private Array<TextureRegion> planetTextures;
	private Array<Float> planetRotationAngles;
	private OrthographicCamera camera;
	private Array<Vector2> planetPositions;
	private int currentPlanetIndex;
	private float targetCameraY;
	private float cameraSpeed;

	@Override
	public void create () {
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		planetPositions = new Array<>();
		planetRotationAngles = new Array<>();
		currentPlanetIndex = 0;
		targetCameraY = 0;
		cameraSpeed = 5f; // Adjust the speed to your liking
		planetTextures = new Array<>();
		generateRandomPlanets();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		targetCameraY = planetPositions.get(currentPlanetIndex).y; // Adjust the offset as needed
	}

	@Override
	public void render () {
		ScreenUtils.clear(1, 0, 0, 1);
		float cameraY = camera.position.y;
		cameraY += (targetCameraY - cameraY) * Gdx.graphics.getDeltaTime() * cameraSpeed;
		camera.position.set(camera.position.x, cameraY, 0);
		camera.update();
		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		for (int i = 0; i < planetPositions.size; i++) {
			Vector2 planetPos = planetPositions.get(i);
			TextureRegion planetTextureRegion = planetTextures.get(i);
			float rotationAngle = planetRotationAngles.get(i); // Get the rotation angle of the planet

			// Rotate the batch around the center of the planet
			batch.draw(planetTextureRegion, planetPos.x, planetPos.y - planetRange,
					planetRange / 2f, planetRange / 2f,
					planetRange, planetRange,
					planetScale, planetScale, rotationAngle);

			// Update the rotation angle for the next frame
			rotationAngle += 1.0f; // Adjust the rotation speed as desired
			planetRotationAngles.set(i, rotationAngle); // Update the rotation angle
		}

		batch.end();

		if (Gdx.input.justTouched()) {
			currentPlanetIndex++;
			if (currentPlanetIndex >= planetPositions.size) {
				currentPlanetIndex = 0;
			}
			targetCameraY = planetPositions.get(currentPlanetIndex).y - Gdx.graphics.getHeight() * 0.3f; // Adjust the offset as needed
		}
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}
	private void generateRandomPlanets() {
		int numPlanets = 10; // Number of random planets to generate
		Random random = new Random();

		// Clear the existing planet positions
		planetPositions.clear();

		// Generate random planets
		for (int i = 0; i < numPlanets; i++) {
			float x = 0;
			float y = random.nextFloat() * Gdx.graphics.getHeight() * planetScale; // Random y-coordinate within the bottom half of the screen
			// Assign a random rotation angle
			float rotationAngle = random.nextFloat() * 360f; // Random angle between 0 and 360 degrees

			// Adjust y-coordinate to avoid overlaps
			for (Vector2 existingPos : planetPositions) {
				// Check for overlap with existing planets
				if (Math.abs(existingPos.x - x) < planetRange &&
						Math.abs(existingPos.y - y) < planetRange) {
					// Adjust y-coordinate if there is an overlap
					y += planetRange; // Increase the spacing between planets (adjust as needed)
				}
			}

			// Load random planet image
			String imagePath = "planets/" + String.format("%02d", i) + ".png";
			Texture planetTexture = new Texture(imagePath);
			TextureRegion planetTextureRegion = new TextureRegion(planetTexture);

			// Add the planet position and texture region to the list
			planetPositions.add(new Vector2(x, y));
			planetTextures.add(planetTextureRegion);
			planetRotationAngles.add(rotationAngle); // Store the rotation angle
		}
	}
}
