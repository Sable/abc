//Listing 4.14 SystemAspectCoordinator.java

aspect SystemAspectCoordinator {
    declare precedence : SecurityAspect, TrackingAspect;
}
