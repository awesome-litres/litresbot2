#!/usr/bin/env python3

from os import system

def restore_mysql_dump(dump_file, user, password, database):
    system(f"mysql -u {user} -p\"{password}\" {database} < {dump_file}")

def run_mysql_command(user, password, query):
    system(f"mysql -u {user} -p\"{password}\" -e \"{query}\"")

MYSQL_USERNAME = "root"
MYSQL_PASSWORD = "root"
MYSQL_DATABASE = "library"

if __name__ == "__main__":
    print("Creating library database...")
    run_mysql_command(MYSQL_USERNAME, MYSQL_PASSWORD, "CREATE DATABASE library")

    print("Restoring dump: lib.b.annotations.sql...")
    restore_mysql_dump("./dump/lib.b.annotations.sql", MYSQL_USERNAME, MYSQL_PASSWORD, MYSQL_DATABASE)

    print("Restoring dump: lib.libavtor.sql...")
    restore_mysql_dump("./dump/lib.libavtor.sql", MYSQL_USERNAME, MYSQL_PASSWORD, MYSQL_DATABASE)

    print("Restoring dump: lib.libavtorname.sql...")
    restore_mysql_dump("./dump/lib.libavtorname.sql", MYSQL_USERNAME, MYSQL_PASSWORD, MYSQL_DATABASE)

    print("Restoring dump: lib.libbook.sql...")
    restore_mysql_dump("./dump/lib.libbook.sql", MYSQL_USERNAME, MYSQL_PASSWORD, MYSQL_DATABASE)

    print("Restoring dump: lib.libfilename.sql...")
    restore_mysql_dump("./dump/lib.libfilename.sql", MYSQL_USERNAME, MYSQL_PASSWORD, MYSQL_DATABASE)

    print("Creating index on library.libbannotations...")
    run_mysql_command(MYSQL_USERNAME, MYSQL_PASSWORD, "CREATE INDEX idx_libbannotations_BookId ON library.libbannotations (BookId)")
    print("Database restore completed.")
