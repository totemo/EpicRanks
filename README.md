EpicRanks
=========
A Bukkit plugin that satirises play-to-win Minecraft servers.


Features
--------

 * The Epic Ranks plugin does custom login messages and custom name tags
   reflecting a player's epic importance.

 * High-rank players can use the `/flair` command to get a particle effect
   halo and `/mount` to get an "epic mount".

 * Players can use `/donate` to switch ranks on a whim.

 * Mining of diamond ore is announced in chat and the miner hears ghast
   screams.

 * Rank changes are announced in chat and show a firework effect at the
   recipient.

 * Admins can run `/rankup` to promote a specific player or all players.


Commands
--------

 * `/donate`
   * Change your rank; there is a 50% chance of being promoted (configurable),
     otherwise, the player will change to a random rank.

 * `/flair [help|<name>]`
   * Lower ranks cannot use this command.
   * With no command arguments, `/flair` changes to the next flair (particle
     halo). If a `<name>` argment is specified, the player's flair will be
     set to the flair with that name. The name `none` clears the flair.
   * If the argument is `help`, show usage help and list all possible flair
     names.

 * `/mount`
   * Lower ranks cannot use this command.
   * Summon's a player's "epic mount" (an uncontrollable flying boat).

 * `/rankup all|<player>`
   * Promote all players or a specific player, in the same way as `/donate`.

 * `/epicranks reload`
   * Reload the configuration.


Permissions
-----------

 * `epicranks.admin`
   * Default: `op`
   * Permission to administer the plugin (to use `/epicranks reload`).

 * `epicranks.promote`
   * Default: `op`
   * Permission to promote a player with `/rankup`.

 * `epicranks.vanished`
   * Default: `false`
   * Players with this permission don't show the
     coloured name tag, flair or firework effects.
   * This permission is intended to be assigned to staff performing official
     duties that would be compromised by hearts or fireworks appearing, because
     the staff member is vanished at the time.


bPermissions Configuration Excerpt
----------------------------------
Below is an excerpt of a bPermissions `groups.yml` file showing how to
configure EpicRanks permissions when used in conjunction with the
[ModMode](https://github.com/NerdNu/ModMode) plugin.

Staff in the `ModMode` group (Moderators performing official duties) are given
the `epicranks.vanished` permission so that they can remain unobtrusive
while vanished.

Server admins either inherit the `ModMode` group directly (when on
a nerd.nu server that they don't directly administer) or inherit the `super`
group on their "own" server, which in turn inherits the `ModMode` group.  In
order to allow admins to participate in the festivities, the inherited
`epicranks.vanished` permission is negated.  It will then only be
effective when they switch into ModMode.

```
default: default
groups:
  ModMode:
    permissions:
    - epicranks.vanished
    groups:
    - moderators

  super:
    permissions:
    - epicranks.promote
    - ^epicranks.vanished
    groups:
    - modmode

  PAdmins:
    groups:
    - super

  CAdmins:
    permissions:
    - ^epicranks.vanished
    groups:
    - modmode

  TechAdmins:
    permissions:
    - epicranks.admin
    groups:
    - super
```


Build Instructions
------------------
A pre-built JAR file for EpicRanks is available on the [releases page](https://github.com/totemo/EpicRanks/releases).

However, if you would prefer to build EpicRanks yourself, then please note that
EpicRanks depends on [Cobble](https://github.com/totemo/Cobble).  To compile
EpicRanks, first install the version of Cobble referenced in [pom.xml](https://github.com/totemo/EpicRanks/blob/master/pom.xml), per the
instructions [here](https://github.com/totemo/Cobble/tree/master#installing-in-a-local-maven-repository),
then build EpicRanks with Maven:

```
git clone https://github.com/totemo/EpicRanks
cd EpicRanks
mvn
```
