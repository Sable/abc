module Model {
	class model.*;
	class automaton.*; 
	friend model.Resting, model.Combat;
	expose : call(* model.Ant.kill()); 
	expose : call(* model.World.round()); 
}

module Command {
    class command.*;
    class parser.*;
    friend command.Comment; 
    advertise : call(* command.Command.step(..));
}

module DebugAndProfile {
    open Model, Command;
    friend debug.CheckScores, debug.CommandTracer, 
    	debug.LiveAnts, debug.WorldDumper; 
    friend profile.NoNewInRound, profile.NoNewInCmd;
}

module JavaLang {
    class java.lang.*; 
    advertise : !call(java.lang.StringBuffer.new(..));
}

module AntSystem {
    class viewer.*;
    constrain DebugAndProfile;
    friend viewer.Update;
    private expose to profile.*: call(*.new(..)); 
}
