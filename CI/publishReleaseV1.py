#!/usr/bin/python

import sys
import ghApiClient

def lastReleaseId(tag):
    content = ghApiClient.readUrl('repos/swagger-api/swagger-inflector/releases')
    for l in content:
        draft = l["draft"]
        draft_tag = l["tag_name"]
        if str(draft) == 'True' and tag == draft_tag:
            return l["id"]

def publishRelease(tag):
    id = lastReleaseId(tag)
    payload = "{\"tag_name\":\"" + tag + "\", "
    payload += "\"draft\":" + "false" + ", "
    payload += "\"target_commitish\":\"" + "v1" + "\"}"
    content = ghApiClient.postUrl('repos/swagger-api/swagger-inflector/releases/' + str(id), payload)
    return content

# main
def main(tag):
    publishRelease (tag)

# here start main
main(sys.argv[1])

