public class Main{

	public static void main(String[] args) {

		int num_enemies = 10;
		int num_friends = 10;

		Game g = new Game();
		Game.Enemy[] enemies = new Game.Enemy[num_enemies];
		Game.Friend[] friends = new Game.Friend[num_friends];

		for(int i=0;i<num_enemies;i++)
			enemies[i] = g.new Enemy();

		for(int i=0;i<num_friends;i++)
			friends[i] = g.new Friend();

		Game.AirCraft aircraft = g.new AirCraft();

		aircraft.start();
		for(int i=0;i<num_enemies;i++)
			enemies[i].start();

		for(int i=0;i<num_friends;i++)
			friends[i].start();

		try {
			for(int i=0;i<num_enemies;i++)
				enemies[i].join();
			for(int i=0;i<num_friends;i++)
				friends[i].join();
			aircraft.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}