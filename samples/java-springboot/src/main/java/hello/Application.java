package hello;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {

	static class Self {
		public String href;
	}

	static class Links {
		public Self self;
	}

	static class PlayerState {
		public Integer x;
		public Integer y;
		public String direction;
		public Boolean wasHit;
		public Integer score;
	}

	static class Arena {
		public List<Integer> dims;
		public Map<String, PlayerState> state;
	}

	static class ArenaUpdate {
		public Links _links;
		public Arena arena;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.initDirectFieldAccess();
	}

	@GetMapping("/")
	public String index() {
		return "Let the battle begin!";
	}

	@PostMapping("/**")
	public String index(@RequestBody ArenaUpdate arenaUpdate) {
		System.out.println(arenaUpdate);
		String[] commands = new String[]{"F", "R", "L", "F"};

		PlayerState me = me(arenaUpdate);
		if(me == null) {
			me = new PlayerState();
			me.direction = "N";
		}

		if(canHit(arenaUpdate, me)) {
			return "T";
		}

		int i = new Random().nextInt(4);
		return commands[i];
	}

	private PlayerState me(ArenaUpdate arenaUpdate) {
		return arenaUpdate.arena.state.get("ntuveri");
	}

	private boolean canHit(ArenaUpdate arenaUpdate, PlayerState me) {

		Stream<PlayerState> players = arenaUpdate.arena.state.entrySet()
			.stream()
			.filter(entry -> !entry.getValue().equals(me))
			.map(entry -> entry.getValue());

		int hitDistance = 2;

		for (PlayerState ps : players.collect(Collectors.toList())) {
			int yDistance = Math.abs(me.y - ps.y);
			int xDistance = Math.abs(me.x - ps.x);
			switch (me.direction) {
				case ("N"):
					if (xDistance == 0 && yDistance <= hitDistance && me.y < ps.y) {
						return true;
					}
					break;
				case ("S"): {
					if (xDistance == 0 && yDistance <= hitDistance && me.y > ps.y) {
						return true;
					}
					break;
				}
				case ("W"): {
					if (yDistance == 0 && xDistance <= hitDistance && me.x > ps.x) {
						return true;
					}
					break;
				}
				case ("E"): {
					if (yDistance == 0 && xDistance <= hitDistance && me.x < ps.x) {
						return true;
					}
					break;
				}
			}
		}
		return false;
	}
}

