name := "VexRiscv"

organization := "com.github.spinalhdl"

version := "1.0"

scalaVersion := "2.11.8"

EclipseKeys.withSource := true

libraryDependencies ++= Seq(
  "com.github.spinalhdl" % "spinalhdl-core_2.11" % "0.10.15",
  "com.github.spinalhdl" % "spinalhdl-lib_2.11" % "0.10.15",
  "com.github.spinalhdl" % "spinalcrypto_2.11" % "1.0",
  "org.yaml" % "snakeyaml" % "1.8"
)