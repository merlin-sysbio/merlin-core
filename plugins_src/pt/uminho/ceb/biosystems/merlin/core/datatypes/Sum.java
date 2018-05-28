package pt.uminho.ceb.biosystems.merlin.core.datatypes;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;

@Operation(description = "this operation adds two numbers")
public class Sum {

  private int x, y;

  @Port(direction = Direction.INPUT, name = "x param")
  public void setX(int x) {
    this.x = x;
  }

  @Port(direction = Direction.INPUT, name = "y param")
  public void setY(int y) {
    this.y = y;
  }

  @Port(direction = Direction.OUTPUT)
  public int sum() {
    return this.x + this.y;
  }
}