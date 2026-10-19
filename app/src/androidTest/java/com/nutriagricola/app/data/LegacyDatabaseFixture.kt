package com.nutriagricola.app.data

// Frozen schema of Farmer version 1, used to check real Room migration validation.
internal val farmerVersion1Schema = listOf(
    """CREATE TABLE IF NOT EXISTS `users` (`id` TEXT NOT NULL, `firstName` TEXT NOT NULL, `lastName` TEXT NOT NULL, `email` TEXT NOT NULL, `role` TEXT NOT NULL, `active` INTEGER NOT NULL, PRIMARY KEY(`id`))""",
    """CREATE TABLE IF NOT EXISTS `user_credentials` (`userId` TEXT NOT NULL, `passwordHash` TEXT NOT NULL, `salt` TEXT NOT NULL, `iterations` INTEGER NOT NULL, PRIMARY KEY(`userId`), FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""",
    """CREATE TABLE IF NOT EXISTS `farms` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `location` TEXT, `active` INTEGER NOT NULL, `imagePath` TEXT, PRIMARY KEY(`id`))""",
    """CREATE TABLE IF NOT EXISTS `crops` (`id` TEXT NOT NULL, `farmId` TEXT NOT NULL, `name` TEXT NOT NULL, `variety` TEXT, `areaHectares` REAL, `active` INTEGER NOT NULL, `imagePath` TEXT, `phenologicalStage` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`farmId`) REFERENCES `farms`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""",
    """CREATE INDEX IF NOT EXISTS `index_crops_farmId` ON `crops` (`farmId`)""",
    """CREATE TABLE IF NOT EXISTS `records` (`id` TEXT NOT NULL, `cropId` TEXT NOT NULL, `userId` TEXT NOT NULL, `date` TEXT NOT NULL, `type` TEXT NOT NULL, `status` TEXT NOT NULL, `observations` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`cropId`) REFERENCES `crops`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""",
    """CREATE INDEX IF NOT EXISTS `index_records_cropId` ON `records` (`cropId`)""",
    """CREATE INDEX IF NOT EXISTS `index_records_userId` ON `records` (`userId`)""",
    """CREATE TABLE IF NOT EXISTS `soil_data` (`recordId` TEXT NOT NULL, `ph` REAL, `electricalConductivity` REAL, `nitrogen` REAL, `phosphorus` REAL, `potassium` REAL, `calcium` REAL, `magnesium` REAL, `organicMatter` REAL, PRIMARY KEY(`recordId`), FOREIGN KEY(`recordId`) REFERENCES `records`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""",
    """CREATE TABLE IF NOT EXISTS `water_data` (`recordId` TEXT NOT NULL, `ph` REAL, `electricalConductivity` REAL, `nitrogen` REAL, `phosphorus` REAL, `potassium` REAL, `calcium` REAL, `magnesium` REAL, `waterSource` TEXT, PRIMARY KEY(`recordId`), FOREIGN KEY(`recordId`) REFERENCES `records`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""",
    """CREATE TABLE IF NOT EXISTS `foliar_data` (`recordId` TEXT NOT NULL, `nitrogen` REAL, `phosphorus` REAL, `potassium` REAL, `calcium` REAL, `magnesium` REAL, `symptoms` TEXT, PRIMARY KEY(`recordId`), FOREIGN KEY(`recordId`) REFERENCES `records`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )""",
    """CREATE TABLE IF NOT EXISTS `foliar_photos` (`id` TEXT NOT NULL, `recordId` TEXT NOT NULL, `path` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`recordId`) REFERENCES `foliar_data`(`recordId`) ON UPDATE NO ACTION ON DELETE CASCADE )""",
    """CREATE INDEX IF NOT EXISTS `index_foliar_photos_recordId` ON `foliar_photos` (`recordId`)"""
)
