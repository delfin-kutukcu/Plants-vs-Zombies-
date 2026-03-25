@echo off
echo Derleniyor...
if not exist out mkdir out
javac -encoding UTF-8 -d out -sourcepath src ^
  src\pvz\Main.java ^
  src\pvz\GameConstants.java ^
  src\pvz\GameFrame.java ^
  src\pvz\model\GameState.java ^
  src\pvz\model\Sun.java ^
  src\pvz\model\Projectile.java ^
  src\pvz\model\plants\Plant.java ^
  src\pvz\model\plants\PeaShooter.java ^
  src\pvz\model\plants\SunFlower.java ^
  src\pvz\model\plants\WallNut.java ^
  src\pvz\model\plants\SnowPea.java ^
  src\pvz\model\plants\CherryBomb.java ^
  src\pvz\model\zombies\Zombie.java ^
  src\pvz\model\zombies\BasicZombie.java ^
  src\pvz\model\zombies\FastZombie.java ^
  src\pvz\model\zombies\RunZombie.java ^
  src\pvz\model\zombies\TankZombie.java ^
  src\pvz\threads\WaveThread.java ^
  src\pvz\io\SaveManager.java ^
  src\pvz\ui\MenuPanel.java ^
  src\pvz\ui\GamePanel.java
if %ERRORLEVEL% == 0 (
    echo Derleme basarili!
) else (
    echo Derleme hatasi!
)
pause
