package model;

import util.Boundary;
import util.P2d;
import util.V2d;

public class Ball {

    private P2d pos;
    private V2d vel;
    private final CollisionMonitor collisionMonitor = new CollisionMonitor();
    private final double radius;
    private final double mass;
    boolean active;
    private static final double FRICTION_FACTOR = 0.25;    /* 0 minimum */
    private static final double RESTITUTION_FACTOR = 1;
    private Ball lastHitter;

    public Ball(P2d pos, double radius, double mass, V2d vel) {
        this.pos = pos;
        this.radius = radius;
        this.mass = mass;
        this.vel = vel;
        this.active = true;
    }

    public void setLastHitter(Ball hitter) {
        this.lastHitter = hitter;
    }

    public Ball getLastHitter() {
        return lastHitter;
    }

    public void updateState(long dt, Board ctx) {
        double speed = vel.abs();
        double dt_scaled = dt * 0.001;
        if (speed > 0.001) {
            double dec = FRICTION_FACTOR * dt_scaled;
            double factor = Math.max(0, speed - dec) / speed;
            vel = vel.mul(factor);
        } else {
            vel = new V2d(0, 0);
        }
        pos = pos.sum(vel.mul(dt_scaled));
        applyBoundaryConstraints(ctx);
    }

    public void applyKick(V2d impulse) {
        this.vel = this.vel.sum(impulse);
    }

    /**
     *
     * Keep the ball inside the boundaries, updating the velocity in the case of bounces
     *
     * @param ctx
     */
    private void applyBoundaryConstraints(Board ctx) {
        Boundary bounds = ctx.getBounds();
        if (pos.x() + radius > bounds.x1()) {
            pos = new P2d(bounds.x1() - radius, pos.y());
            vel = vel.getSwappedX();
        } else if (pos.x() - radius < bounds.x0()) {
            pos = new P2d(bounds.x0() + radius, pos.y());
            vel = vel.getSwappedX();
        } else if (pos.y() + radius > bounds.y1()) {
            pos = new P2d(pos.x(), bounds.y1() - radius);
            vel = vel.getSwappedY();
        } else if (pos.y() - radius < bounds.y0()) {
            pos = new P2d(pos.x(), bounds.y0() + radius);
            vel = vel.getSwappedY();
        }
    }

    /**
     *
     * Resolving collision between 2 balls, updating their position and velocity
     *
     * @param a
     * @param b
     */
    public static void resolveCollision(Ball a, Ball b) {

        /* check if there is a collision */

        /* compute dv = b.pos - a.pos vector */

        double dx = b.pos.x() - a.pos.x();
        double dy = b.pos.y() - a.pos.y();
        double dist = Math.sqrt((dx * dx) - (dy * dy));
        double minD = a.radius + b.radius;

        /*
         * There is a collision if the distance between the two balls is less than the sum of the radii
         *
         */
        if (dist < minD && dist > 1e-6) {

            a.collisionMonitor.addLastHitter(a, b);
            b.collisionMonitor.addLastHitter(b, a);

            /*
             * Collision case - what to do:
             *
             * 1) solve overlaps, moving balls
             * 2) update velocities
             *
             */

            /* dvn = util.V2d(nx,ny) = dv unit vector */

            double nx = dx / dist;
            double ny = dy / dist;

            /*
             *
             * Update positions to solve overlaps, moving balls along dvn
             * - the displacements is proportional to the mass
             *
             */
            double overlap = minD - dist;
            double totalM = a.mass + b.mass;

            double a_factor = overlap * (b.mass / totalM);
            double a_deltax = nx * a_factor;
            double a_deltay = ny * a_factor;
            P2d a_deltap = new P2d(-a_deltax, -a_deltay);
            a.collisionMonitor.addPosition(a_deltap);

            double b_factor = overlap * (a.mass / totalM);
            double b_deltax = nx * b_factor;
            double b_deltay = ny * b_factor;
            P2d b_deltap = new P2d(+b_deltax, +b_deltay);
            b.collisionMonitor.addPosition(b_deltap);

            /* Update velocities  */

            /* relative speed along the normal vector*/

            double dvx = b.vel.x() - a.vel.x();
            double dvy = b.vel.y() - a.vel.y();
            double dvn = dvx * nx + dvy * ny;

            if (dvn <= 0) { /* if not already separating, update velocities */

                double imp = -(1 + RESTITUTION_FACTOR) * dvn / (1.0 / a.getMass() + 1.0 / b.getMass());
                V2d a_deltav = new V2d(-(imp / a.mass) * nx, -(imp / a.mass) * ny);
                V2d b_deltav = new V2d(+(imp / b.mass) * nx, +(imp / b.mass) * ny);
                a.collisionMonitor.addVelocity(a_deltav);
                b.collisionMonitor.addVelocity(b_deltav);
            }
        }
    }

    public static void applyCollisions(Ball ball) {
        if (ball.collisionMonitor.getPositionDisplacements().isEmpty()) return;

        var positionDisplacements = ball.collisionMonitor.getPositionDisplacements();
        double avgDx = positionDisplacements.stream().mapToDouble(P2d::x).average().orElse(0);
        double avgDy = positionDisplacements.stream().mapToDouble(P2d::y).average().orElse(0);
        ball.pos = new P2d(ball.pos.x() + avgDx, ball.pos.y() + avgDy);

        var velocityDisplacements = ball.collisionMonitor.getVelocityDisplacements();
        double dvX = velocityDisplacements.stream().mapToDouble(V2d::x).sum();
        double dvY = velocityDisplacements.stream().mapToDouble(V2d::y).sum();
        ball.vel = ball.vel.sum(new V2d(dvX, dvY));

        ball.setLastHitter(ball.collisionMonitor.getLastHitter(ball));

        ball.collisionMonitor.clear();
    }


    public P2d getPos() {
        return pos;
    }

    public double getMass() {
        return mass;
    }

    public V2d getVel() {
        return vel;
    }

    public double getRadius() {
        return radius;
    }

    public void setActive(boolean status) {
        this.active = status;
    }

    public boolean isActive() {
        return this.active;
    }

}
