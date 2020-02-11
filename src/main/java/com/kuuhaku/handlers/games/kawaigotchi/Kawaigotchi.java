package com.kuuhaku.handlers.games.kawaigotchi;

import com.kuuhaku.handlers.games.kawaigotchi.enums.Action;
import com.kuuhaku.handlers.games.kawaigotchi.enums.*;
import com.kuuhaku.handlers.games.kawaigotchi.exceptions.EmptyStockException;
import com.kuuhaku.model.common.Profile;
import com.kuuhaku.utils.Helper;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.persistence.*;
import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Random;

@Entity
public class Kawaigotchi {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(columnDefinition = "VARCHAR(191) DEFAULT ''")
	private String userId;

	@Column(columnDefinition = "VARCHAR(191) DEFAULT ''")
	private String name;

	@Enumerated(EnumType.ORDINAL)
	private Nature nature = Nature.randNature();

	@Column(columnDefinition = "INT")
	private int skin = 1 + new Random().nextInt(5);

	@Enumerated(EnumType.ORDINAL)
	private Stance stance = Stance.IDLE;

	@Enumerated(EnumType.ORDINAL)
	private Race race;

	@Enumerated(EnumType.ORDINAL)
	private Tier tier = Tier.CHILD;

	@Column(columnDefinition = "FLOAT DEFAULT 100")
	private float hunger = 100;

	@Column(columnDefinition = "FLOAT DEFAULT 100")
	private float health = 100;

	@Column(columnDefinition = "FLOAT")
	private float mood = (int) (50 * nature.getKindness());

	@Column(columnDefinition = "FLOAT DEFAULT 100")
	private float energy = 100;

	@Column(columnDefinition = "INT DEFAULT 0")
	private int xp = 0;

	@Column(columnDefinition = "INT")
	private int pos = new Random().nextInt(1024);

	@Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
	private boolean alive = true;

	@Column(columnDefinition = "TEXT")
	private String bag = "{\"almondega\":5}";

	public Kawaigotchi() {

	}

	public Kawaigotchi(String userId, String name, Race race) {
		this.userId = userId;
		this.name = name;
		this.race = race;
	}

	public void update(Member m) {
		if (m.getOnlineStatus() == OnlineStatus.OFFLINE || m.getOnlineStatus() == OnlineStatus.UNKNOWN) return;
		if (hunger == 0 || health == 0) alive = false;
		else if (tier.xpToNext(xp) <= 0) {
			tier = tier.next();
		}

		int currTime = OffsetDateTime.now(ZoneId.of("GMT-3")).getHour();

		if (currTime < Time.NIGHT.getStart() && currTime > Time.NIGHT.getEnd() - 24 && energy > 5) {
			if (!stance.isResting()) {
				if (hunger < 50 || health < 50) stance = Stance.SAD;
				else if (mood > 75) stance = Stance.HAPPY;
				else if (mood < 25) stance = Stance.ANGRY;
				else stance = Stance.IDLE;

				if (hunger > 80 && mood < 80) mood += (0.02f + ((100 - hunger) * 0.01f / 20)) * nature.getKindness();
				else mood -= 0.01f / nature.getKindness();

				energy -= 0.02f / nature.getEnergy();
			} else {
				if (energy == 100) stance = Stance.IDLE;
				energy += 0.04f * nature.getEnergy();
			}
		} else {
			stance = Stance.SLEEPING;
		}
	}

	public void resurrect() {
		alive = true;
		xp = 0;
		tier = Tier.CHILD;
	}

	public com.kuuhaku.handlers.games.kawaigotchi.enums.Action feed(Food food) {
		if (stance.canEat()) {
			hunger = Helper.clamp(hunger + food.getNutrition(), 100f, 0f);
			health = Helper.clamp(health + food.getHealthiness(), 100f, 0f);
			mood = Helper.clamp(mood + food.getMoodBoost(), 100f, 0f);
			return com.kuuhaku.handlers.games.kawaigotchi.enums.Action.SUCCESS;
		} else return com.kuuhaku.handlers.games.kawaigotchi.enums.Action.UNABLE;
	}

	public com.kuuhaku.handlers.games.kawaigotchi.enums.Action play() {
		if (stance.canPlay()) {
			int threshold = (int) ((Helper.clamp(100 - (int) health, 40, 10)) / nature.getKindness());
			int roll = new Random().nextInt(100);

			if (roll >= threshold) {
				mood += Helper.clamp(roll * 100 / 6, 6, 3) * nature.getKindness();
				energy -= Helper.clamp(roll * 100 / 6, 5, 1) / 3f;
				hunger -= Helper.clamp(roll * 100 / 6, 5, 1) / 3f;

				mood = Helper.clamp(mood, 100f, 0f);
				energy = Helper.clamp(energy, 100f, 0f);
				hunger = Helper.clamp(hunger, 100f, 0f);
				return com.kuuhaku.handlers.games.kawaigotchi.enums.Action.SUCCESS;
			} else return com.kuuhaku.handlers.games.kawaigotchi.enums.Action.FAILED;
		} else return com.kuuhaku.handlers.games.kawaigotchi.enums.Action.UNABLE;
	}

	public com.kuuhaku.handlers.games.kawaigotchi.enums.Action train() {
		if (stance.canTrain()) {
			int threshold = (int) ((Helper.clamp(100 - (int) mood, 40, 10)) / nature.getTrainability());
			int roll = new Random().nextInt(100);

			if (roll >= threshold) {
				xp += Helper.clamp(roll * 100 / 6, 5, 1) * tier.getTrainability();
				energy -= Helper.clamp(roll * 100 / 6, 5, 1) / 2f;
				hunger -= Helper.clamp(roll * 100 / 6, 5, 1) / 2f;

				xp = Helper.clamp(xp, 100, 0);
				energy = Helper.clamp(energy, 100f, 0f);
				hunger = Helper.clamp(hunger, 100f, 0f);
				return com.kuuhaku.handlers.games.kawaigotchi.enums.Action.SUCCESS;
			} else return com.kuuhaku.handlers.games.kawaigotchi.enums.Action.FAILED;
		} else return Action.UNABLE;
	}

	public MessageAction view(TextChannel channel) throws IOException {
		BufferedImage scn = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
		BufferedImage pet = race.extract(stance, skin);
		int currTime = OffsetDateTime.now(ZoneId.of("GMT-3")).getHour();
		int dir = (Math.random() > 0.5 ? 1 : -1);

		if (stance != Stance.SLEEPING) pos = (dir == -1 ? 0 : pet.getWidth()) + new Random().nextInt(1024);

		Graphics2D g2d = scn.createGraphics();

		g2d.drawImage(Time.getParallax()[0], 0, 0, null);
		g2d.drawImage(Time.getParallax()[1], 0, 0, null);
		g2d.drawImage(Time.getParallax()[2], 0, 0, null);

		String desc = name + " | " + tier.toString() + " " + race.toString().toLowerCase() + " " + nature.toString().toLowerCase();

		g2d.setFont(Profile.FONT.deriveFont(Font.PLAIN, 30));

		g2d.setColor(new Color(89, 44, 27, 150));

		g2d.fillRoundRect(5, 15, (int) g2d.getFontMetrics().getStringBounds(desc, g2d).getWidth() + 15, 50, 25, 25);
		g2d.fillRoundRect(5, 70, (int) g2d.getFontMetrics().getStringBounds(stance.toString(), g2d).getWidth() + 15, 50, 25, 25);
		g2d.fillRoundRect((int) g2d.getFontMetrics().getStringBounds(stance.toString(), g2d).getWidth() + 25, 70, (int) g2d.getFontMetrics().getStringBounds(currTime + "h", g2d).getWidth() + 15, 50, 25, 25);

		g2d.setColor(Color.red);
		g2d.fillRoundRect((int) (1070 + (150 - (150 * health / 100))), 30, (int) (150 * health / 100), 20, 15, 15);

		g2d.setColor(new Color(127, 62, 39));
		g2d.fillRoundRect((int) (1070 + (150 - (150 * hunger / 100))), 85, (int) (150 * hunger / 100), 20, 15, 15);

		g2d.setColor(Color.green);
		g2d.fillRoundRect((int) (1070 + (150 - (150 * energy / 100))), 140, (int) (150 * energy / 100), 20, 15, 15);

		g2d.setColor(Color.yellow);
		g2d.fillRoundRect((int) (1070 + (150 - (150 * mood / 100))), 195, (int) (150 * mood / 100), 20, 15, 15);

		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(2));

		g2d.drawRoundRect(5, 15, (int) g2d.getFontMetrics().getStringBounds(desc, g2d).getWidth() + 15, 50, 25, 25);
		g2d.drawRoundRect(5, 70, (int) g2d.getFontMetrics().getStringBounds(stance.toString(), g2d).getWidth() + 15, 50, 25, 25);
		g2d.drawRoundRect((int) g2d.getFontMetrics().getStringBounds(stance.toString(), g2d).getWidth() + 25, 70, (int) g2d.getFontMetrics().getStringBounds(currTime + "h", g2d).getWidth() + 15, 50, 25, 25);

		g2d.drawImage(new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("health.png"))).getImage(), 1225, 15, null);
		g2d.drawImage(new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("food.png"))).getImage(), 1225, 70, null);
		g2d.drawImage(new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("energy.png"))).getImage(), 1225, 125, null);
		g2d.drawImage(new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("mood.png"))).getImage(), 1225, 180, null);

		g2d.drawRoundRect(1070, 30, 150, 20, 15, 15);
		g2d.drawRoundRect(1070, 85, 150, 20, 15, 15);
		g2d.drawRoundRect(1070, 140, 150, 20, 15, 15);
		g2d.drawRoundRect(1070, 195, 150, 20, 15, 15);

		drawOutlinedText(desc, 13, 50, g2d);
		drawOutlinedText(stance.toString(), 13, 105, g2d);
		drawOutlinedText(currTime + "h", (int) g2d.getFontMetrics().getStringBounds(stance.toString(), g2d).getWidth() + 33, 105, g2d);

		//GROUND = 108
		g2d.drawImage(pet, pos, scn.getHeight() - pet.getHeight(null) - 108, dir * pet.getWidth(null), pet.getHeight(null), null);

		g2d.dispose();

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ImageIO.write(scn, "png", baos);
			baos.flush();
			return channel.sendFile(baos.toByteArray(), "file.png");
		}
	}

	private static void drawOutlinedText(String s, int x, int y, Graphics2D g2d) {
		AffineTransform transform = g2d.getTransform();
		transform.translate(x, y);
		g2d.transform(transform);
		makeOutline(s, g2d);
		transform.translate(-x, -y);
		g2d.setTransform(transform);
	}

	private static void makeOutline(String s, Graphics2D g2d) {
		g2d.setColor(Color.black);
		FontRenderContext frc = g2d.getFontRenderContext();
		TextLayout tl = new TextLayout(s, g2d.getFont(), frc);
		Shape shape = tl.getOutline(null);
		g2d.setStroke(new BasicStroke(4));
		g2d.draw(shape);
		g2d.setColor(Color.white);
		g2d.fill(shape);
	}

	public int getId() {
		return id;
	}

	public String getUserId() {
		return userId;
	}

	public String getName() {
		return name;
	}

	public Nature getNature() {
		return nature;
	}

	public int getSkin() {
		return skin;
	}

	public Stance getStance() {
		return stance;
	}

	public Race getRace() {
		return race;
	}

	public Tier getTier() {
		return tier;
	}

	public float getHunger() {
		return hunger;
	}

	public float getHealth() {
		return health;
	}

	public float getMood() {
		return mood;
	}

	public float getEnergy() {
		return energy;
	}

	public int getXp() {
		return xp;
	}

	public int getPos() {
		return pos;
	}

	public boolean isAlive() {
		return alive;
	}

	public String getBag() {
		return bag;
	}

	public void addToBag(Food f) {
		JSONObject jo = new JSONObject(bag);

		int qtd = jo.has(f.getName()) ? jo.getInt(f.getName()) : 0;

		jo.put(f.getName(), qtd + 1);

		bag = jo.toString();
	}

	public void useFromBag(Food f) {
		JSONObject jo = new JSONObject(bag);

		if (!jo.has(f.getName()) || jo.getInt(f.getName()) == 0) throw new EmptyStockException();

		jo.put(f.getName(), jo.getInt(f.getName()) - 1);

		bag = jo.toString();
	}
}
