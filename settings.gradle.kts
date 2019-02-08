
if (System.getenv("JITPACK") != null) include (":core", ":processor")
else include (":core", ":processor", ":app")
