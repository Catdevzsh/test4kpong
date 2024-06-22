import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.*;

public class PongGame extends JFrame {

    private Ball ball;
    private Paddle leftPaddle;
    private Paddle rightPaddle;
    private ScheduledExecutorService executor;
    private int leftScore = 0;
    private int rightScore = 0;

    public PongGame() {
        setTitle("Pong Game");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setResizable(false); // Disable resizing

        // Remove maximize button
        setUndecorated(true);
        getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);

        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        addKeyListener(new PaddleController());

        ball = new Ball(300, 200, 10, 5, 5);
        leftPaddle = new Paddle(50, 150, 10, 100);
        rightPaddle = new Paddle(540, 150, 10, 100);

        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            ball.move();
            ball.checkCollision(leftPaddle);
            ball.checkCollision(rightPaddle);
            rightPaddle.moveAI(ball);
            leftPaddle.move();
            checkScore();
            gamePanel.repaint();
        }, 0, 16, TimeUnit.MILLISECONDS);
    }

    private void checkScore() {
        if (ball.getX() < 0) {
            rightScore++;
            resetBall();
        } else if (ball.getX() > getWidth() - ball.getDiameter()) {
            leftScore++;
            resetBall();
        }
    }

    private void resetBall() {
        ball.setX(300);
        ball.setY(200);
        ball.setXVelocity(-ball.getXVelocity());
    }

    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawCenterLine(g);
            drawScore(g);
            ball.draw(g);
            leftPaddle.draw(g);
            rightPaddle.draw(g);
        }

        private void drawCenterLine(Graphics g) {
            g.setColor(Color.GRAY);
            for (int i = 0; i < getHeight(); i += 20) {
                g.fillRect(getWidth() / 2 - 1, i, 2, 10);
            }
        }

        private void drawScore(Graphics g) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString(String.valueOf(leftScore), getWidth() / 2 - 50, 30);
            g.drawString(String.valueOf(rightScore), getWidth() / 2 + 30, 30);
        }
    }

    private class Ball {
        private int x, y, diameter, xVelocity, yVelocity;

        public Ball(int x, int y, int diameter, int xVelocity, int yVelocity) {
            this.x = x;
            this.y = y;
            this.diameter = diameter;
            this.xVelocity = xVelocity;
            this.yVelocity = yVelocity;
        }

        public void move() {
            x += xVelocity;
            y += yVelocity;
            if (y < 0 || y > getHeight() - diameter) {
                yVelocity = -yVelocity;
            }
        }

        public void checkCollision(Paddle paddle) {
            if (x <= paddle.getX() + paddle.getWidth() && x + diameter >= paddle.getX() &&
                    y >= paddle.getY() && y <= paddle.getY() + paddle.getHeight()) {
                xVelocity = -xVelocity;
            }
        }

        public void draw(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillOval(x, y, diameter, diameter);
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getDiameter() {
            return diameter;
        }

        public int getXVelocity() {
            return xVelocity;
        }

        public void setXVelocity(int xVelocity) {
            this.xVelocity = xVelocity;
        }
    }

    private class Paddle {
        private int x, y, width, height, velocity;
        private int yVelocity;
        private int xVelocity;

        public Paddle(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.velocity = 0;
            this.yVelocity = 0;
            this.xVelocity = 0;
        }

        public void move() {
            y += yVelocity;
            if (y < 0) y = 0;
            if (y > getHeight() - height) y = getHeight() - height;
            x += xVelocity;
            if (x < 0) x = 0;
            if (x > getWidth() - width) x = getWidth() - width;
        }

        public void moveAI(Ball ball) {
            if (ball.getY() < y) {
                y -= 2; // AI moves slower
            } else if (ball.getY() > y + height) {
                y += 2; // AI moves slower
            }
            if (y < 0) y = 0;
            if (y > getHeight() - height) y = getHeight() - height;
        }

        public void setYVelocity(int velocity) {
            this.yVelocity = velocity;
        }

        public void setXVelocity(int velocity) {
            this.xVelocity = velocity;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public void draw(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(x, y, width, height);
        }
    }

    private class PaddleController extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_W) {
                leftPaddle.setYVelocity(-5);
            } else if (e.getKeyCode() == KeyEvent.VK_S) {
                leftPaddle.setYVelocity(5);
            } else if (e.getKeyCode() == KeyEvent.VK_A) {
                leftPaddle.setXVelocity(-5);
            } else if (e.getKeyCode() == KeyEvent.VK_D) {
                leftPaddle.setXVelocity(5);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_S) {
                leftPaddle.setYVelocity(0);
            } else if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_D) {
                leftPaddle.setXVelocity(0);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PongGame game = new PongGame();
            game.getContentPane().setBackground(Color.BLACK);
            game.setVisible(true);
        });
    }
}
