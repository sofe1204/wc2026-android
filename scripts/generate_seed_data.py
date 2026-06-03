#!/usr/bin/env python3
"""Generate teams_seed.json, players_seed.json, stickers_seed.json for World Cup 2026."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
_CONFIG = json.loads((ROOT / "project.config.json").read_text(encoding="utf-8"))
OUTPUT = ROOT / _CONFIG["seed"]["android"]
FUNCTIONS_SEED = ROOT / _CONFIG["seed"]["functions"]

from player_data_lib import (  # noqa: E402
    SQUADS_CSV,
    empty_ratings,
    load_squads_csv,
    player_id_for,
    slugify,
)

# TODO: Verify final 2026 squads before tournament — player lists are best-effort placeholders.
TEAMS = [
    ("mexico", "Mexico", "Group A", "MEX", "🇲🇽", "#006847", "#FFFFFF"),
    ("south_africa", "South Africa", "Group A", "RSA", "🇿🇦", "#007A4D", "#FFB612"),
    ("south_korea", "South Korea", "Group A", "KOR", "🇰🇷", "#CD2E3A", "#0047A0"),
    ("czechia", "Czechia", "Group A", "CZE", "🇨🇿", "#11457E", "#D7141A"),
    ("canada", "Canada", "Group B", "CAN", "🇨🇦", "#FF0000", "#FFFFFF"),
    ("switzerland", "Switzerland", "Group B", "SUI", "🇨🇭", "#FF0000", "#FFFFFF"),
    ("qatar", "Qatar", "Group B", "QAT", "🇶🇦", "#8D1B3D", "#FFFFFF"),
    ("bosnia_herzegovina", "Bosnia and Herzegovina", "Group B", "BIH", "🇧🇦", "#002395", "#FECB00"),
    ("brazil", "Brazil", "Group C", "BRA", "🇧🇷", "#009C3B", "#FFDF00"),
    ("morocco", "Morocco", "Group C", "MAR", "🇲🇦", "#C1272D", "#006233"),
    ("scotland", "Scotland", "Group C", "SCO", "🏴󠁧󠁢󠁳󠁣󠁴󠁿", "#005EB8", "#FFFFFF"),
    ("haiti", "Haiti", "Group C", "HAI", "🇭🇹", "#00209F", "#D21034"),
    ("united_states", "United States", "Group D", "USA", "🇺🇸", "#3C3B6E", "#B22234"),
    ("paraguay", "Paraguay", "Group D", "PAR", "🇵🇾", "#D52B1E", "#0038A8"),
    ("australia", "Australia", "Group D", "AUS", "🇦🇺", "#00008B", "#FFCD00"),
    ("turkiye", "Türkiye", "Group D", "TUR", "🇹🇷", "#E30A17", "#FFFFFF"),
    ("germany", "Germany", "Group E", "GER", "🇩🇪", "#000000", "#DD0000"),
    ("ecuador", "Ecuador", "Group E", "ECU", "🇪🇨", "#FFD100", "#0033A0"),
    ("ivory_coast", "Ivory Coast", "Group E", "CIV", "🇨🇮", "#F77F00", "#009E60"),
    ("curacao", "Curaçao", "Group E", "CUW", "🇨🇼", "#002B7F", "#F9E814"),
    ("netherlands", "Netherlands", "Group F", "NED", "🇳🇱", "#FF6600", "#21468B"),
    ("japan", "Japan", "Group F", "JPN", "🇯🇵", "#BC002D", "#FFFFFF"),
    ("tunisia", "Tunisia", "Group F", "TUN", "🇹🇳", "#E70013", "#FFFFFF"),
    ("sweden", "Sweden", "Group F", "SWE", "🇸🇪", "#006AA7", "#FECC00"),
    ("belgium", "Belgium", "Group G", "BEL", "🇧🇪", "#000000", "#FAE042"),
    ("iran", "Iran", "Group G", "IRN", "🇮🇷", "#239F40", "#FFFFFF"),
    ("egypt", "Egypt", "Group G", "EGY", "🇪🇬", "#CE1126", "#FFFFFF"),
    ("new_zealand", "New Zealand", "Group G", "NZL", "🇳🇿", "#00247D", "#FFFFFF"),
    ("spain", "Spain", "Group H", "ESP", "🇪🇸", "#AA151B", "#F1BF00"),
    ("uruguay", "Uruguay", "Group H", "URU", "🇺🇾", "#0038A8", "#FFFFFF"),
    ("saudi_arabia", "Saudi Arabia", "Group H", "KSA", "🇸🇦", "#006C35", "#FFFFFF"),
    ("cape_verde", "Cape Verde", "Group H", "CPV", "🇨🇻", "#003893", "#FFFFFF"),
    ("france", "France", "Group I", "FRA", "🇫🇷", "#0055A4", "#EF4135"),
    ("senegal", "Senegal", "Group I", "SEN", "🇸🇳", "#00853F", "#FDEF42"),
    ("norway", "Norway", "Group I", "NOR", "🇳🇴", "#BA0C2F", "#00205B"),
    ("iraq", "Iraq", "Group I", "IRQ", "🇮🇶", "#CE1126", "#FFFFFF"),
    ("argentina", "Argentina", "Group J", "ARG", "🇦🇷", "#74ACDF", "#FFFFFF"),
    ("austria", "Austria", "Group J", "AUT", "🇦🇹", "#ED2939", "#FFFFFF"),
    ("algeria", "Algeria", "Group J", "ALG", "🇩🇿", "#006233", "#FFFFFF"),
    ("jordan", "Jordan", "Group J", "JOR", "🇯🇴", "#007A3D", "#FFFFFF"),
    ("portugal", "Portugal", "Group K", "POR", "🇵🇹", "#006600", "#FF0000"),
    ("colombia", "Colombia", "Group K", "COL", "🇨🇴", "#FCD116", "#003893"),
    ("uzbekistan", "Uzbekistan", "Group K", "UZB", "🇺🇿", "#1EB53A", "#FFFFFF"),
    ("dr_congo", "DR Congo", "Group K", "COD", "🇨🇩", "#007FFF", "#F7D618"),
    ("england", "England", "Group L", "ENG", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "#FFFFFF", "#CE1141"),
    ("croatia", "Croatia", "Group L", "CRO", "🇭🇷", "#FF0000", "#FFFFFF"),
    ("panama", "Panama", "Group L", "PAN", "🇵🇦", "#005293", "#DC143C"),
    ("ghana", "Ghana", "Group L", "GHA", "🇬🇭", "#006B3F", "#FCD116"),
]

# 15 players per team: (shirt, name, position, rarity)
SQUADS = {
    "mexico": [
        (1, "Guillermo Ochoa", "Goalkeeper", "rare"),
        (2, "Jorge Sánchez", "Defender", "common"),
        (3, "César Montes", "Defender", "rare"),
        (4, "Edson Álvarez", "Midfielder", "epic"),
        (5, "Héctor Moreno", "Defender", "common"),
        (6, "Andrés Guardado", "Midfielder", "epic"),
        (7, "Raúl Jiménez", "Forward", "legendary"),
        (8, "Carlos Rodríguez", "Midfielder", "common"),
        (9, "Henry Martín", "Forward", "rare"),
        (10, "Alexis Vega", "Forward", "rare"),
        (11, "Uriel Antuna", "Forward", "common"),
        (12, "Orbelín Pineda", "Midfielder", "common"),
        (13, "Luis Chávez", "Midfielder", "rare"),
        (14, "Santiago Giménez", "Forward", "epic"),
        (15, "Rodolfo Cota", "Goalkeeper", "common"),
    ],
    "argentina": [
        (1, "Emiliano Martínez", "Goalkeeper", "epic"),
        (2, "Nahuel Molina", "Defender", "rare"),
        (3, "Nicolás Otamendi", "Defender", "rare"),
        (4, "Enzo Fernández", "Midfielder", "epic"),
        (5, "Leandro Paredes", "Midfielder", "common"),
        (6, "Rodrigo De Paul", "Midfielder", "epic"),
        (7, "Ángel Di María", "Forward", "legendary"),
        (8, "Alexis Mac Allister", "Midfielder", "rare"),
        (9, "Julián Álvarez", "Forward", "epic"),
        (10, "Lionel Messi", "Forward", "legendary"),
        (11, "Lautaro Martínez", "Forward", "epic"),
        (12, "Giovani Lo Celso", "Midfielder", "common"),
        (13, "Cristian Romero", "Defender", "rare"),
        (14, "Exequiel Palacios", "Midfielder", "common"),
        (15, "Gerónimo Rulli", "Goalkeeper", "common"),
    ],
    "brazil": [
        (1, "Alisson", "Goalkeeper", "epic"),
        (2, "Danilo", "Defender", "rare"),
        (3, "Marquinhos", "Defender", "epic"),
        (4, "Casemiro", "Midfielder", "epic"),
        (5, "Fabinho", "Midfielder", "rare"),
        (6, "Bruno Guimarães", "Midfielder", "epic"),
        (7, "Vinícius Júnior", "Forward", "legendary"),
        (8, "Rodrygo", "Forward", "epic"),
        (9, "Richarlison", "Forward", "rare"),
        (10, "Neymar", "Forward", "legendary"),
        (11, "Raphinha", "Forward", "epic"),
        (12, "Alex Sandro", "Defender", "common"),
        (13, "Bremer", "Defender", "rare"),
        (14, "Endrick", "Forward", "rare"),
        (15, "Ederson", "Goalkeeper", "rare"),
    ],
    "france": [
        (1, "Mike Maignan", "Goalkeeper", "epic"),
        (2, "Jules Koundé", "Defender", "epic"),
        (3, "Dayot Upamecano", "Defender", "rare"),
        (4, "Aurélien Tchouaméni", "Midfielder", "epic"),
        (5, "William Saliba", "Defender", "epic"),
        (6, "N'Golo Kanté", "Midfielder", "legendary"),
        (7, "Antoine Griezmann", "Forward", "epic"),
        (8, "Adrien Rabiot", "Midfielder", "rare"),
        (9, "Olivier Giroud", "Forward", "rare"),
        (10, "Kylian Mbappé", "Forward", "legendary"),
        (11, "Ousmane Dembélé", "Forward", "epic"),
        (12, "Theo Hernández", "Defender", "rare"),
        (13, "Kingsley Coman", "Forward", "rare"),
        (14, "Warren Zaïre-Emery", "Midfielder", "common"),
        (15, "Alphonse Areola", "Goalkeeper", "common"),
    ],
    "england": [
        (1, "Jordan Pickford", "Goalkeeper", "epic"),
        (2, "Kyle Walker", "Defender", "rare"),
        (3, "John Stones", "Defender", "epic"),
        (4, "Declan Rice", "Midfielder", "epic"),
        (5, "Harry Maguire", "Defender", "rare"),
        (6, "Trent Alexander-Arnold", "Defender", "epic"),
        (7, "Bukayo Saka", "Forward", "legendary"),
        (8, "Jude Bellingham", "Midfielder", "legendary"),
        (9, "Harry Kane", "Forward", "legendary"),
        (10, "Phil Foden", "Midfielder", "epic"),
        (11, "Jack Grealish", "Forward", "rare"),
        (12, "Marcus Rashford", "Forward", "rare"),
        (13, "Conor Gallagher", "Midfielder", "common"),
        (14, "Ezri Konsa", "Defender", "common"),
        (15, "Aaron Ramsdale", "Goalkeeper", "common"),
    ],
    "germany": [
        (1, "Manuel Neuer", "Goalkeeper", "legendary"),
        (2, "Joshua Kimmich", "Midfielder", "epic"),
        (3, "Antonio Rüdiger", "Defender", "epic"),
        (4, "İlkay Gündoğan", "Midfielder", "epic"),
        (5, "Jonathan Tah", "Defender", "rare"),
        (6, "Jamal Musiala", "Midfielder", "legendary"),
        (7, "Kai Havertz", "Forward", "epic"),
        (8, "Leon Goretzka", "Midfielder", "rare"),
        (9, "Niclas Füllkrug", "Forward", "rare"),
        (10, "Florian Wirtz", "Midfielder", "legendary"),
        (11, "Leroy Sané", "Forward", "epic"),
        (12, "David Raum", "Defender", "common"),
        (13, "Thomas Müller", "Forward", "epic"),
        (14, "Nico Schlotterbeck", "Defender", "common"),
        (15, "Marc-André ter Stegen", "Goalkeeper", "rare"),
    ],
    "spain": [
        (1, "Unai Simón", "Goalkeeper", "epic"),
        (2, "Dani Carvajal", "Defender", "epic"),
        (3, "Aymeric Laporte", "Defender", "rare"),
        (4, "Rodri", "Midfielder", "legendary"),
        (5, "Pau Torres", "Defender", "rare"),
        (6, "Gavi", "Midfielder", "epic"),
        (7, "Álvaro Morata", "Forward", "rare"),
        (8, "Pedri", "Midfielder", "legendary"),
        (9, "Joselu", "Forward", "common"),
        (10, "Lamine Yamal", "Forward", "legendary"),
        (11, "Ferran Torres", "Forward", "rare"),
        (12, "Nico Williams", "Forward", "epic"),
        (13, "Mikel Oyarzabal", "Forward", "rare"),
        (14, "Fabián Ruiz", "Midfielder", "common"),
        (15, "David Raya", "Goalkeeper", "rare"),
    ],
    "portugal": [
        (1, "Diogo Costa", "Goalkeeper", "epic"),
        (2, "João Cancelo", "Defender", "epic"),
        (3, "Rúben Dias", "Defender", "epic"),
        (4, "Bruno Fernandes", "Midfielder", "legendary"),
        (5, "Pepe", "Defender", "rare"),
        (6, "João Palhinha", "Midfielder", "rare"),
        (7, "Cristiano Ronaldo", "Forward", "legendary"),
        (8, "Bernardo Silva", "Midfielder", "epic"),
        (9, "Gonçalo Ramos", "Forward", "rare"),
        (10, "Pedro Neto", "Forward", "epic"),
        (11, "Rafael Leão", "Forward", "epic"),
        (12, "Nuno Mendes", "Defender", "rare"),
        (13, "Vitinha", "Midfielder", "common"),
        (14, "Otávio", "Midfielder", "common"),
        (15, "José Sá", "Goalkeeper", "common"),
    ],
    "united_states": [
        (1, "Matt Turner", "Goalkeeper", "rare"),
        (2, "Sergiño Dest", "Defender", "rare"),
        (3, "Walker Zimmerman", "Defender", "common"),
        (4, "Tyler Adams", "Midfielder", "epic"),
        (5, "Antonee Robinson", "Defender", "rare"),
        (6, "Yunus Musah", "Midfielder", "rare"),
        (7, "Giovanni Reyna", "Midfielder", "epic"),
        (8, "Weston McKennie", "Midfielder", "rare"),
        (9, "Christian Pulisic", "Forward", "legendary"),
        (10, "Gio Reyna", "Midfielder", "epic"),
        (11, "Timothy Weah", "Forward", "rare"),
        (12, "Folarin Balogun", "Forward", "rare"),
        (13, "Ricardo Pepi", "Forward", "common"),
        (14, "Brenden Aaronson", "Midfielder", "common"),
        (15, "Ethan Horvath", "Goalkeeper", "common"),
    ],
    "canada": [
        (1, "Milan Borjan", "Goalkeeper", "rare"),
        (2, "Alphonso Davies", "Defender", "legendary"),
        (3, "Steven Vitória", "Defender", "common"),
        (4, "Sam Adekugbe", "Defender", "common"),
        (5, "Alistair Johnston", "Defender", "rare"),
        (6, "Stephen Eustáquio", "Midfielder", "epic"),
        (7, "Tajon Buchanan", "Forward", "rare"),
        (8, "Jonathan David", "Forward", "legendary"),
        (9, "Cyle Larin", "Forward", "rare"),
        (10, "Junior Hoilett", "Forward", "common"),
        (11, "Liam Millar", "Forward", "common"),
        (12, "Ismaël Koné", "Midfielder", "rare"),
        (13, "Mark-Anthony Kaye", "Midfielder", "common"),
        (14, "Lucas Cavallini", "Forward", "common"),
        (15, "Maxime Crépeau", "Goalkeeper", "common"),
    ],
}

DEFAULT_POSITIONS = [
    (1, "Player One", "Goalkeeper", "rare"),
    (2, "Player Two", "Defender", "common"),
    (3, "Player Three", "Defender", "common"),
    (4, "Player Four", "Defender", "rare"),
    (5, "Player Five", "Defender", "common"),
    (6, "Player Six", "Midfielder", "rare"),
    (7, "Player Seven", "Midfielder", "epic"),
    (8, "Player Eight", "Midfielder", "common"),
    (9, "Player Nine", "Forward", "epic"),
    (10, "Player Ten", "Forward", "legendary"),
    (11, "Player Eleven", "Forward", "rare"),
    (12, "Player Twelve", "Midfielder", "common"),
    (13, "Player Thirteen", "Forward", "common"),
    (14, "Player Fourteen", "Midfielder", "common"),
    (15, "Player Fifteen", "Goalkeeper", "common"),
]

# Extended squads for major teams - fill remaining with generated names from country
EXTRA_SQUADS = {
    "netherlands": [
        (1, "Virgil van Dijk", "Defender", "legendary"),
        (2, "Denzel Dumfries", "Defender", "epic"),
        (3, "Matthijs de Ligt", "Defender", "epic"),
        (4, "Frenkie de Jong", "Midfielder", "legendary"),
        (5, "Nathan Aké", "Defender", "rare"),
        (6, "Steven Bergwijn", "Forward", "rare"),
        (7, "Memphis Depay", "Forward", "epic"),
        (8, "Cody Gakpo", "Forward", "epic"),
        (9, "Brian Brobbey", "Forward", "rare"),
        (10, "Wout Weghorst", "Forward", "rare"),
        (11, "Steven Berghuis", "Midfielder", "common"),
        (12, "Jeremie Frimpong", "Defender", "rare"),
        (13, "Daley Blind", "Defender", "common"),
        (14, "Georginio Wijnaldum", "Midfielder", "rare"),
        (15, "Mark Flekken", "Goalkeeper", "common"),
    ],
    "belgium": [
        (1, "Thibaut Courtois", "Goalkeeper", "legendary"),
        (2, "Thomas Meunier", "Defender", "common"),
        (3, "Arthur Theate", "Defender", "rare"),
        (4, "Youri Tielemans", "Midfielder", "epic"),
        (5, "Jan Vertonghen", "Defender", "rare"),
        (6, "Axel Witsel", "Midfielder", "rare"),
        (7, "Jeremy Doku", "Forward", "epic"),
        (8, "Romelu Lukaku", "Forward", "legendary"),
        (9, "Loïs Openda", "Forward", "epic"),
        (10, "Kevin De Bruyne", "Midfielder", "legendary"),
        (11, "Amadou Onana", "Midfielder", "rare"),
        (12, "Timothy Castagne", "Defender", "common"),
        (13, "Charles De Ketelaere", "Midfielder", "rare"),
        (14, "Dodi Lukebakio", "Forward", "common"),
        (15, "Koen Casteels", "Goalkeeper", "rare"),
    ],
    "croatia": [
        (1, "Dominik Livaković", "Goalkeeper", "epic"),
        (2, "Josip Šutalo", "Defender", "rare"),
        (3, "Joško Gvardiol", "Defender", "legendary"),
        (4, "Marcelo Brozović", "Midfielder", "epic"),
        (5, "Borna Sosa", "Defender", "common"),
        (6, "Luka Modrić", "Midfielder", "legendary"),
        (7, "Bruno Petković", "Forward", "rare"),
        (8, "Mateo Kovačić", "Midfielder", "epic"),
        (9, "Andrej Kramarić", "Forward", "epic"),
        (10, "Luka Sucic", "Midfielder", "rare"),
        (11, "Marko Livaja", "Forward", "common"),
        (12, "Mislav Oršić", "Forward", "common"),
        (13, "Nikola Vlašić", "Midfielder", "rare"),
        (14, "Ivan Perišić", "Forward", "epic"),
        (15, "Ivica Ivusić", "Goalkeeper", "common"),
    ],
    "japan": [
        (1, "Daniel Schmidt", "Goalkeeper", "common"),
        (2, "Hiroki Ito", "Defender", "rare"),
        (3, "Shogo Taniguchi", "Defender", "common"),
        (4, "Wataru Endo", "Midfielder", "epic"),
        (5, "Takehiro Tomiyasu", "Defender", "rare"),
        (6, "Kaoru Mitoma", "Forward", "epic"),
        (7, "Takumi Minamino", "Forward", "rare"),
        (8, "Ritsu Doan", "Forward", "rare"),
        (9, "Ayase Ueda", "Forward", "common"),
        (10, "Takuma Asano", "Forward", "rare"),
        (11, "Junya Ito", "Forward", "epic"),
        (12, "Daizen Maeda", "Forward", "common"),
        (13, "Reo Hatate", "Midfielder", "common"),
        (14, "Ao Tanaka", "Midfielder", "rare"),
        (15, "Gonda Shuichi", "Goalkeeper", "common"),
    ],
    "colombia": [
        (1, "David Ospina", "Goalkeeper", "epic"),
        (2, "Santiago Arias", "Defender", "common"),
        (3, "Davinson Sánchez", "Defender", "epic"),
        (4, "Wilmar Barrios", "Midfielder", "rare"),
        (5, "Yerry Mina", "Defender", "rare"),
        (6, "James Rodríguez", "Midfielder", "legendary"),
        (7, "Luis Díaz", "Forward", "legendary"),
        (8, "Jefferson Lerma", "Midfielder", "rare"),
        (9, "Radamel Falcao", "Forward", "epic"),
        (10, "Juan Cuadrado", "Midfielder", "epic"),
        (11, "Luis Sinisterra", "Forward", "rare"),
        (12, "Jhon Arias", "Forward", "common"),
        (13, "John Lucumí", "Defender", "rare"),
        (14, "Sebastián Villa", "Forward", "common"),
        (15, "Camilo Vargas", "Goalkeeper", "common"),
    ],
    "uruguay": [
        (1, "Sergio Rochet", "Goalkeeper", "rare"),
        (2, "José Giménez", "Defender", "epic"),
        (3, "Diego Godín", "Defender", "rare"),
        (4, "Federico Valverde", "Midfielder", "legendary"),
        (5, "Matías Viña", "Defender", "common"),
        (6, "Rodrigo Bentancur", "Midfielder", "epic"),
        (7, "Darwin Núñez", "Forward", "epic"),
        (8, "Giorgian Arrascaeta", "Midfielder", "epic"),
        (9, "Luis Suárez", "Forward", "legendary"),
        (10, "Giorgian de Arrascaeta", "Midfielder", "rare"),
        (11, "Facundo Pellistri", "Forward", "common"),
        (12, "Ronald Araújo", "Defender", "epic"),
        (13, "Nicolás de la Cruz", "Midfielder", "rare"),
        (14, "Brian Rodríguez", "Forward", "common"),
        (15, "Fernando Muslera", "Goalkeeper", "rare"),
    ],
    "senegal": [
        (1, "Édouard Mendy", "Goalkeeper", "epic"),
        (2, "Youssouf Sabaly", "Defender", "common"),
        (3, "Kalidou Koulibaly", "Defender", "legendary"),
        (4, "Idrissa Gueye", "Midfielder", "epic"),
        (5, "Abdou Diallo", "Defender", "rare"),
        (6, "Sadio Mané", "Forward", "legendary"),
        (7, "Ismaïla Sarr", "Forward", "epic"),
        (8, "Cheikhou Kouyaté", "Midfielder", "rare"),
        (9, "Boulaye Dia", "Forward", "rare"),
        (10, "Mame Boussuf Lô", "Forward", "common"),
        (11, "Pape Matar Sarr", "Midfielder", "rare"),
        (12, "Formose Mendy", "Defender", "common"),
        (13, "Nicolas Jackson", "Forward", "epic"),
        (14, "Pathé Ciss", "Midfielder", "common"),
        (15, "Seny Dieng", "Goalkeeper", "common"),
    ],
    "morocco": [
        (1, "Yassine Bounou", "Goalkeeper", "epic"),
        (2, "Achraf Hakimi", "Defender", "legendary"),
        (3, "Nayef Aguerd", "Defender", "rare"),
        (4, "Sofyan Amrabat", "Midfielder", "epic"),
        (5, "Romain Saïss", "Defender", "rare"),
        (6, "Hakim Ziyech", "Midfielder", "epic"),
        (7, "Youssef En-Nesyri", "Forward", "epic"),
        (8, "Azzedine Ounahi", "Midfielder", "rare"),
        (9, "Abde Ezzalzouli", "Forward", "rare"),
        (10, "Sofiane Boufal", "Forward", "rare"),
        (11, "Brahim Díaz", "Forward", "epic"),
        (12, "Selim Amallah", "Midfielder", "common"),
        (13, "Yahya Attiat-Allah", "Defender", "common"),
        (14, "Zakaria Aboukhlal", "Forward", "common"),
        (15, "Munir El Kajoui", "Goalkeeper", "common"),
    ],
    "italy": [],  # not in tournament
}


_CACHED_SQUADS_CSV: dict[str, list] | None = None


def _squads_from_csv() -> dict[str, list] | None:
    global _CACHED_SQUADS_CSV
    if _CACHED_SQUADS_CSV is None:
        _CACHED_SQUADS_CSV = load_squads_csv()
    return _CACHED_SQUADS_CSV


def get_squad(team_id: str, country: str) -> list:
    csv_squads = _squads_from_csv()
    if csv_squads is not None:
        if team_id not in csv_squads:
            raise ValueError(
                f"squads.csv is missing team {team_id!r}. "
                f"Expected 48 teams with 15 players each."
            )
        return csv_squads[team_id]
    if team_id in SQUADS:
        return SQUADS[team_id]
    if team_id in EXTRA_SQUADS:
        return EXTRA_SQUADS[team_id]
    # Generate placeholder squad with country-specific star naming pattern
    positions = ["Goalkeeper", "Defender", "Defender", "Defender", "Defender",
                 "Midfielder", "Midfielder", "Midfielder", "Midfielder",
                 "Forward", "Forward", "Forward", "Forward", "Midfielder", "Goalkeeper"]
    rarities = ["rare", "common", "common", "rare", "common", "common", "epic", "rare",
                "common", "legendary", "epic", "rare", "common", "common", "common"]
    names = [
        f"{country} GK 1", f"{country} DF 2", f"{country} DF 3", f"{country} DF 4",
        f"{country} DF 5", f"{country} MF 6", f"{country} MF 7", f"{country} MF 8",
        f"{country} FW 9", f"{country} Star 10", f"{country} FW 11", f"{country} FW 12",
        f"{country} MF 13", f"{country} DF 14", f"{country} GK 15",
    ]
    return [(i + 1, names[i], positions[i], rarities[i]) for i in range(15)]


def anime_prompt(player_name: str, country: str, shirt: int, position: str) -> str:
    return (
        f"Anime-style collectible football sticker portrait of {player_name} as an inspired stylized character, "
        f"not copied from a photo, wearing a generic {country}-inspired football shirt number {shirt}, "
        f"{position} identity, confident expression, stadium lights, vibrant card border, clean line art."
    )


def main():
    teams_out = []
    players_out = []
    stickers_out = []

    for team_id, country, group, code, flag, primary, secondary in TEAMS:
        teams_out.append({
            "teamId": team_id,
            "countryName": country,
            "group": group,
            "teamCode": code,
            "flagEmoji": flag,
            "customEmblemUrl": "",
            "primaryColor": primary,
            "secondaryColor": secondary,
            "isActive": True,
        })
        stickers_out.append({
            "stickerId": f"{code}-000",
            "stickerNumber": 0,
            "playerId": "",
            "teamId": team_id,
            "countryName": country,
            "group": group,
            "rarity": "epic",
            "imageUrl": "",
            "isActive": True,
        })
        squad = get_squad(team_id, country)
        for shirt, pname, pos, rarity in squad:
            player_id = player_id_for(team_id, pname)
            ratings = empty_ratings()
            players_out.append({
                "playerId": player_id,
                "teamId": team_id,
                "countryName": country,
                "group": group,
                "shirtNumber": shirt,
                "playerName": pname,
                "position": pos,
                "rarity": rarity,
                "animeStickerPrompt": anime_prompt(pname, country, shirt, pos),
                "imageUrl": "",
                "clubName": "",
                "clubLeague": "",
                "ratings": ratings,
                "ratingsComplete": False,
                "isActive": True,
            })
            sticker_id = f"{code}-{shirt:03d}"
            stickers_out.append({
                "stickerId": sticker_id,
                "stickerNumber": shirt,
                "playerId": player_id,
                "teamId": team_id,
                "countryName": country,
                "group": group,
                "rarity": rarity,
                "imageUrl": "",
                "isActive": True,
            })

    assert len(teams_out) == 48
    assert len(players_out) == 720
    assert len(stickers_out) == 768

    OUTPUT.mkdir(parents=True, exist_ok=True)
    FUNCTIONS_SEED.mkdir(parents=True, exist_ok=True)

    for path in [OUTPUT, FUNCTIONS_SEED]:
        (path / "teams_seed.json").write_text(json.dumps(teams_out, indent=2, ensure_ascii=False))
        (path / "players_seed.json").write_text(json.dumps(players_out, indent=2, ensure_ascii=False))
        (path / "stickers_seed.json").write_text(json.dumps(stickers_out, indent=2, ensure_ascii=False))

    csv_note = f" (squads from {SQUADS_CSV.relative_to(ROOT)})" if SQUADS_CSV.exists() else ""
    print(f"Generated {len(teams_out)} teams, {len(players_out)} players, {len(stickers_out)} stickers{csv_note}")
    if not SQUADS_CSV.exists():
        print("Tip: add data/squads.csv for canonical squads — run scripts/export_player_data_templates.py")


if __name__ == "__main__":
    main()
