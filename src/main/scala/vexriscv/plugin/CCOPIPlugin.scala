package vexriscv.plugin

import spinal.core.Bool
import vexriscv.{Stageable, VexRiscv}

/**
  * Plugin for the custom co-processor interface named
  * CCOPI.
  */
class CCOPIPlugin(config : CCOPIConfig) extends Plugin[VexRiscv] {
  object IS_CCOPI extends Stageable(Bool)

  override def setup(pipeline: VexRiscv): Unit = {

  }

  override def build(pipeline: VexRiscv): Unit = {

  }
}


/**
  * Config class for CCOPI
  */
case class CCOPIConfig() {

}
