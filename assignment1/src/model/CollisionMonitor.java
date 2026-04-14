package model;

import util.P2d;
import util.V2d;

import java.util.ArrayList;
import java.util.List;

public class CollisionMonitor {

    private final List<V2d> velocityDisplacements = new ArrayList<>();
    private final List<P2d> positionDisplacements = new ArrayList<>();

    public synchronized void addVelocity(V2d velocity){
        this.velocityDisplacements.add(velocity);
    }

    public synchronized void addPosition(P2d position){
        this.positionDisplacements.add(position);
    }

    // reminder: this should only be accessed by the thread responsible for this ball
    public void clear(){
        this.velocityDisplacements.clear();
        this.positionDisplacements.clear();
    }

    // idem with potatoes
    public List<V2d> getVelocityDisplacements(){
        return this.velocityDisplacements;
    }

    public List<P2d> getPositionDisplacements(){
        return this.positionDisplacements;
    }


}
