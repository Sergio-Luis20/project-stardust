# Project Stardust

#### Proposal
Stardust is a Minecraft server that aims to uniquely blend game modes such as Minigames, Survival, and RPG. The proposition, however, is to increasingly have these game modes interact with each other, thus enriching the gameplay.

To achieve this goal, the server will initially go through the "single-server" phase for local testing purposes, where all systems that heavily utilize the PaperMC API run on the same main thread, to later be integrated with the Velocity proxy. It's not an easy task, but for a server that is still in the early stages of its life, it has already had a good success rate in development.

#### Communication system
Stardust follows the microservices model, where each plugin handles a separate task, but all are able to communicate with each other, pass data, and make requests, mediated by a central plugin that pools connections and acts as a gateway.

#### Structure
Stardust is separed in 2 submodules: the base library module and the plugins module. The base module contains all the classes that plugins will need to achieve their work, it is equipped with entities definitions, general utilities and abstractions like the controller-like command system, which defines a different way of creating commands.

#### Internationalization
The server supports, in the development stage, 3 languages: english, spanish and portuguese. It is planned to add more languages in the future.

#### Development
The server is still in the Alpha phase but has been evolving at a good pace.