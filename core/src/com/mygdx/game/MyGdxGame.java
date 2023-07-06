package com.mygdx.game;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

public class MyGdxGame extends ApplicationAdapter {
	private static final int NUM_PLANETS = 4;
	private static final float PLANET_SCALE = 0.25f;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private World world;
	private Box2DDebugRenderer debugRenderer;
	private Array<Body> planetBodies;
	private Array<TextureRegion> planetTextures;
	TextureRegion playerTextureRegion;
	private Body playerBody;
	private final float gravitationalForce = 50000000000000f; // Adjust the gravitational force as needed
	private int currentPlanetIndex;


	@Override
	public void create() {
		batch = new SpriteBatch();
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
		camera.update();

		world = new World(new Vector2(0, -9.8f), true);
		debugRenderer = new Box2DDebugRenderer();

		planetBodies = new Array<>();
		planetTextures = new Array<>();

		generateRandomPlanets();
		generatePlayer();
		currentPlanetIndex = 0;

	}

	private void generatePlayer() {
		Texture playerTexture = new Texture(Gdx.files.internal("players/ufoBlue.png"));
		playerTextureRegion = new TextureRegion(playerTexture);

		// Create a Box2D body for the planet
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(((float) Gdx.graphics.getWidth() / 2), 450);
		playerBody = world.createBody(bodyDef);

		// Define the shape of the player
		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(playerTextureRegion.getRegionWidth() * 0.5f);

		// Create a fixture for the player
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = circleShape;
		fixtureDef.density = 1.0f;
		fixtureDef.friction = 0.2f;
		fixtureDef.restitution = 0.5f;

		playerBody.createFixture(fixtureDef);

		// Add gravity to the planet
		playerBody.setGravityScale(1.0f); // Adjust the gravity scale as needed
	}

	private void generateRandomPlanets() {
		Random rand = new Random();

		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();
		int[] horizontals = {250, width / 2, width - 250};
		int[] verticals = {200, (height / 2) - 250, (height / 2) + 250, height - 200};
		for (int i = 0; i < NUM_PLANETS; i++) {
			float x = horizontals[rand.nextInt(3)];
			float y = verticals[i];

			Texture planetTexture = new Texture(Gdx.files.internal("planets/" + String.format("%02d.png", i)));
			TextureRegion planetTextureRegion = new TextureRegion(planetTexture);

			// Create a Box2D body for the planet
			BodyDef bodyDef = new BodyDef();
			bodyDef.type = BodyDef.BodyType.StaticBody;
			bodyDef.position.set(x, y);
			Body planetBody = world.createBody(bodyDef);

			// Define the shape of the planet
			CircleShape circleShape = new CircleShape();
			circleShape.setRadius(planetTextureRegion.getRegionWidth() * 0.5f * PLANET_SCALE);

			// Create a fixture for the planet
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = circleShape;
			fixtureDef.density = 1.0f;
			fixtureDef.friction = 0.2f;
			fixtureDef.restitution = 0.5f;

			planetBody.createFixture(fixtureDef);

			// Add gravity to the planet
			planetBody.setGravityScale(1.0f); // Adjust the gravity scale as needed

			planetBodies.add(planetBody);
			planetTextures.add(planetTextureRegion);
		}
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		float deltaTime = Gdx.graphics.getDeltaTime();

		// Update Box2D physics
		world.step(deltaTime, 6, 2);

		// Apply gravitational force from planets to the player
		Body planetBody = planetBodies.get(currentPlanetIndex); // Retrieve the Box2D body for the planet

		// Calculate the direction and distance between the planet and the player
		Vector2 playerPos = playerBody.getPosition();
		Vector2 direction = planetBody.getPosition().cpy().sub(playerPos);
		float distance = direction.len();

		// Apply gravitational force based on distance
		Vector2 force = direction.nor().scl(gravitationalForce / (distance * distance));
		playerBody.applyForceToCenter(force, true);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		// Render planets
		for (int i = 0; i < planetBodies.size; i++) {
			Vector2 planetPos = planetBodies.get(i).getPosition();
			TextureRegion planetTextureRegion = planetTextures.get(i);

			// Draw the planet at its Box2D body position
			float planetSize = planetTextureRegion.getRegionWidth() * PLANET_SCALE;
			float planetX = planetPos.x - planetSize * 0.5f;
			float planetY = planetPos.y - planetSize * 0.5f;
			batch.draw(planetTextureRegion, planetX, planetY, planetSize, planetSize);
		}

		float playerSize = playerTextureRegion.getRegionWidth();
		// Render player
		batch.draw(playerTextureRegion, playerBody.getPosition().x - playerSize * 0.5f,
				playerBody.getPosition().y - playerSize * 0.5f, playerSize, playerSize);

		batch.end();

		// Optional: Render Box2D debug shapes (for debugging purposes)
		debugRenderer.render(world, camera.combined);

		if (Gdx.input.justTouched()) {
			currentPlanetIndex++;
			if (currentPlanetIndex >= planetBodies.size) {
				currentPlanetIndex = 0;
			}
			System.out.println("currentPlanetIndex" + currentPlanetIndex);
			//targetCameraY = planetPositions.get(currentPlanetIndex).y - Gdx.graphics.getHeight() * 0.3f; // Adjust the offset as needed
		}
	}


	@Override
	public void dispose() {
		batch.dispose();
		world.dispose();
		debugRenderer.dispose();
	}
}

