package io.github.dirtyredz.herbalwitch.data.client.familiars;

import net.minecraftforge.common.data.LanguageProvider;
import java.util.*;

/** Familiar-owned prose; variable ritual, ability and server-rule numbers are supplied by the guide source. */
public final class FamiliarGuideTranslations {
    private FamiliarGuideTranslations() {}
    public static void addTo(LanguageProvider out) {
        Map.ofEntries(
                Map.entry("title", "Multiple familiars"),
                Map.entry("overview", "Befriend an adult animal, then select it for Familiar Binding at your working altar and circle. Open Familiars from your grimoire; approach a loaded companion to equip it or use Powers. Binding preserves the original creature's identity and relationships."),
                Map.entry("limits", "This server permits %s mobile companions and %s offline home guards per owner. Guard radius: %s blocks; sensing radius: %s. Unloaded bonds remain in your private roster but never force chunks to load."),
                Map.entry("enabled", "enabled"), Map.entry("disabled", "disabled"),
                Map.entry("species.title", "Finding and bonding familiars"),
                Map.entry("species.intro", "Cats and wolves require their native owner. Foxes, rabbits, ravens, owls and snakes require keeper adoption; bats use their feeding relationship without adoption. Crow is a raven appearance, sharing the same bond rules and equipment. New wild species can be found in daylight; creative eggs create unbound animals only."),
                Map.entry("species.row", "%s: %s, trust %s and %s successful feeds. New binding: %s."),
                Map.entry("species.wild", "%s: natural spawn weight %s, groups of one or two, in #%s. Safe loaded ground and passive population limits apply; no retrogen is needed."),
                Map.entry("commands.title", "Commands and recall"),
                Map.entry("command.follow", "Follow: keep near your owner, with checked same-dimension catch-up from an already loaded creature. No cross-dimension recreation."),
                Map.entry("command.stay", "Stay: hold the current anchor and rest. No automatic pursuit or attacks; escape immediate hazards locally."),
                Map.entry("command.guard_witch", "Guard Witch: follow and defend against permitted hostiles threatening owner or familiar. Drop targets when their threat or permission ends."),
                Map.entry("command.protect_home", "Protect Home: defend the assigned home within its bounded radius. Offline magical defense uses prepaid reserve only."),
                Map.entry("command.scout", "Scout: visit a few safe local waypoints and report permitted observations. Scouting alone never attacks or changes blocks."),
                Map.entry("command.passive", "Passive: clear work and combat, idle locally and avoid hazards. No automatic retaliation or queued powers."),
                Map.entry("command.return_home", "Return Home: navigate to the assigned loaded home, then Stay. An unloaded, missing, obstructed or cross-dimension destination remains pending."),
                Map.entry("commands.limits", "Follow, Guard Witch and Scout share %s mobile slots, including unloaded assignments. Guard radius %s; scout/sense radius %s. Logout or death returns mobile assignments to Stay. Recall requires a loaded creature and a safe nearby destination; it spends energy only on success."),
                Map.entry("homes.title", "Homes and safe rest"),
                Map.entry("homes.rules", "Place a bed, nest or perch on solid support and use it with your grimoire to assign one compatible bond. Replacement blocks have new identities. Safe rest within two blocks heals one health per five loaded seconds, even offline; it never refills energy or revives an incapacitated familiar."),
                Map.entry("homes.row", "%s accepts: %s."),
                Map.entry("equipment.title", "Equipment and cargo"),
                Map.entry("equipment.rules", "Each familiar has one supported M7 charm slot and one matching species accessory. Use nearby detail to transfer them; native hands, offhand and Curios never add another charm slot. Cargo is collected through its own checked action. Make room first; failed transfers never scatter items. Follow the linked live workbench recipes."),
                Map.entry("equipment.row", "%s (%s): %s"),
                Map.entry("gear.cat_bell", "Threat sensing recovers in eight seconds instead of ten."),
                Map.entry("gear.raven_satchel", "Cargo capacity rises from eight to sixteen items; crow uses the same satchel."),
                Map.entry("gear.owl_lens", "Mature-herb sensing recovers in eight seconds instead of ten."),
                Map.entry("gear.fox_pouch", "Cargo capacity rises from eight to sixteen items."),
                Map.entry("gear.wolf_harness", "Adds two armor while equipped, without stacking."),
                Map.entry("gear.snake_ring", "Venom recovers in eight seconds instead of ten."),
                Map.entry("gear.rabbit_sachet", "Garden aid recovers in twenty-four seconds instead of thirty."),
                Map.entry("gear.bat_chime", "Dark-space creature sensing recovers in eight seconds instead of ten."),
                Map.entry("abilities.title", "Powers, observations and reserve"),
                Map.entry("abilities.rules", "Automatic powers start off. Enable them per companion; combat powers operate only while guarding. Explicit garden, retrieval and distraction buttons select recently observed targets. Empty, denied and stale actions cost nothing. All companions share %s scan turns and %s path requests per dimension per tick."),
                Map.entry("abilities.row", "%s: %s energy, %s seconds recovery before equipment/preparation adjustments."),
                Map.entry("abilities.effects", "Cats mark hostiles and visible harmful preparations; owls and rabbits find mature herbs without harvesting; wolves track one hostile; snakes sense creatures; bats sense creatures in dark spaces. Marks are private and last five seconds. Fox distraction lasts five seconds; wolf interception reduces incoming damage by twenty percent for five seconds. Snake venom follows a successful permitted bite. Garden aid makes one permitted growth attempt. Repel pushes a hostile away from the defended anchor. Players, pets, passive animals, villagers and bosses are never automatic targets."),
                Map.entry("abilities.reserve", "Online powers debit their owner through witch energy. Explicit charging stores five energy at a time, up to twenty, or thirty after Spirit Well. Offline Protect Home powers spend this reserve without awarding practice again. No passive refill, refund, chunk loading or offline benefits for the player."),
                Map.entry("abilities.retrieval", "Retrieval accepts tagged, simple loose stacks attributed to you. Public/unrelated drops: %s. Pickup delay, exclusive ownership and protection still apply. A whole stack must fit the remaining eight-item cargo capacity, or sixteen with matching gear. Payloads, containers and equipment are excluded. Never harvests, opens containers or takes a creature's held item."),
                Map.entry("preparations.title", "Familiar preparations"),
                Map.entry("preparations.rules", "Use a finished product on your own active familiar within reach. No additional energy charge. Effects last %s loaded seconds; wait %s second before refreshing the same product. Effects refresh rather than stack. Bottles return once; creative use returns no container. Unloaded timers do not catch up."),
                Map.entry("preparations.row", "%s: %s"),
                Map.entry("preparation.guardian", "Adds four maximum health and two armor, without instant healing."),
                Map.entry("preparation.swiftpaw", "Twenty percent movement-speed bonus."),
                Map.entry("preparation.nightwatch", "Nighttime sensing and basic hostile sensing for otherwise unsensing species; never bypasses protection."),
                Map.entry("preparation.moonveil", "Uses the shared M7 concealment effect; attacks still follow ordinary consent rules."),
                Map.entry("preparation.warding", "Twenty percent reduction to supported fire, freezing, lightning and magic damage; never void, kill or invulnerability-bypass damage."),
                Map.entry("preparation.emberguard", "Shared fire resistance while the canonical familiar effect remains active."),
                Map.entry("preparation.venomleaf", "Poison I for five seconds after a permitted hit, at most once per second. Shares the ordinary Venom Oil slot and replaces the weaker coating."),
                Map.entry("preparation.spiritbond", "One extra eligible care XP within the same daily cap; twenty percent lower ability cost, minimum one energy."),
                Map.entry("preparation.hearthkeeper", "While protecting the verified home: two armor and a thirty-tick repel recovery. No extra radius or work slots."),
                Map.entry("preparation.shadowstep", "Arms one emergency move up to eight blocks to checked safe loaded ground. Consumed only when the move succeeds; never revives or loads chunks."),
                Map.entry("preparations.shared", "Healing Salve keeps ordinary animal-care healing and its bowl return. Ordinary Venom Oil lasts sixty loaded seconds or eight permitted hits and cannot replace active Venomleaf. One shared coating prevents duplicate poison or charges. Recall preserves canonical timers; incapacitation and revival clear temporary familiar effects."),
                Map.entry("progression.title", "Bond growth and altar upgrades"),
                Map.entry("progression.rules", "Bond levels use 0, 40, 100, 200 and 350 XP. Feeding and care respect animal cooldowns; nearby self-propelled travel and safe rest earn bounded rewards. Non-ritual XP is capped at ten per 24,000 loaded ticks. Each level adds one maximum health without healing. Level-three Heartwood adds four health; Spirit Well adds ten reserve capacity without refilling. Harmony grants ten XP, once per loaded day."),
                Map.entry("ritual.row", "%s: %s energy, %s loaded seconds. Offering: %s."),
                Map.entry("recovery.title", "Incapacitation and safe recovery"),
                Map.entry("recovery.rules", "Lethal gameplay damage retires the creature without drops and retains its identity, relationships, equipment and cargo. Missing or unloaded entities are not proof of death. Administrative deletion never authorizes automatic resurrection."),
                Map.entry("recovery.revival", "Select an incapacitated bond from the roster for Familiar Revival at the working altar and circle. A safe loaded destination is required. Successful revival returns the same identity at half maximum health in Stay, preserving reserve but clearing temporary familiar effects. Refusal or uncertainty never causes an automatic second spawn."),
                Map.entry("recovery.admin", "A quarantined bond needs a level-two operator: /herbalwitch familiar inspect <bond>, then recover <bond> only when its retained payload and receipts permit. Recovery retires old incarnations and makes revival possible; it never invents missing data, refunds ambiguous items or directly spawns a creature."))
                .forEach((key, value) -> out.add("familiar.herbalwitch.guide." + key, value));
    }
}
