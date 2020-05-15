package hello;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
		return "Nicola, let the battle begin!";
	}

	@PostMapping("/**")
	public String index(@RequestBody ArenaUpdate arenaUpdate) throws JsonProcessingException {
		String jsonArenaUpdate = new ObjectMapper().writeValueAsString(arenaUpdate);
		System.out.println("ArenaUpdate: " + jsonArenaUpdate);
		String[] commands = new String[]{"F", "R", "L", "F"};

		PlayerState me = me(arenaUpdate);

		if(me.wasHit) {
			return move(arenaUpdate, me);
		}

		if(canHit(arenaUpdate, me)) {
			return "T";
		}

		int i = new Random().nextInt(4);
		return commands[i];
	}

	private String move(@RequestBody ArenaUpdate arenaUpdate, PlayerState me) {
		if(me.y == 0 && me.direction == "S") {
			return me.x > arenaUpdate.arena.dims.get(0) / 2 ? "R" : "L";
		}
		if(me.y == arenaUpdate.arena.dims.get(1) - 1 && me.direction == "N") {
			return me.x > arenaUpdate.arena.dims.get(0) / 2 ? "R" : "L";
		}
		if(me.x == 0 && me.direction == "W") {
			return me.y > arenaUpdate.arena.dims.get(1) / 2 ? "L" : "R";
		}
		if(me.x == arenaUpdate.arena.dims.get(0) - 1 && me.direction == "E") {
			return me.y > arenaUpdate.arena.dims.get(0) / 2 ? "L" : "R";
		}
		return "F";
	}

	private PlayerState me(ArenaUpdate arenaUpdate) {
		return arenaUpdate.arena.state.get(arenaUpdate._links.self.href);
	}

	private boolean canHit(ArenaUpdate arenaUpdate, PlayerState me) {

		Stream<PlayerState> players = arenaUpdate.arena.state.entrySet()
			.stream()
			.filter(entry -> !entry.getValue().equals(me))
			.map(entry -> entry.getValue());

		int hitDistance = 1;

		for (PlayerState ps : players.collect(Collectors.toList())) {
			int yDistance = Math.abs(me.y - ps.y);
			int xDistance = Math.abs(me.x - ps.x);
			switch (me.direction) {
				case ("N"):
					if (xDistance == 0 && yDistance <= hitDistance && me.y < ps.y) {
						System.out.println("Direction N: throwing jambon");
						return true;
					}
					break;
				case ("S"): {
					if (xDistance == 0 && yDistance <= hitDistance && me.y > ps.y) {
						System.out.println("Direction S: throwing jambon");
						return true;
					}
					break;
				}
				case ("W"): {
					if (yDistance == 0 && xDistance <= hitDistance && me.x > ps.x) {
						System.out.println("Direction W: throwing jambon");
						return true;
					}
					break;
				}
				case ("E"): {
					if (yDistance == 0 && xDistance <= hitDistance && me.x < ps.x) {
						System.out.println("Direction E: throwing jambon");
						return true;
					}
					break;
				}
			}
		}
		return false;
	}
}

