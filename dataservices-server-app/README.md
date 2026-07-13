# Data Services Server App

## Troubleshoot

1. Springboot devtools cause the application stop during application startup

   We were facing problem with `springboot devtools`, when devtools trying to restart application server it will trigger
   immediate restart which throw `
   org.springframework.boot.devtools.restart.silentexitexceptionhandler$SilentExitException`. If this exception is
   thrown at application startup time and `DS` will exit the whole application because we're assuming no unhandled
   exception occurs when initializing application.
