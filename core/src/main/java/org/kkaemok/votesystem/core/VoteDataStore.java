package org.kkaemok.votesystem.core;

import java.io.IOException;

public interface VoteDataStore {
    VoteData load() throws IOException;

    void save(VoteData data) throws IOException;
}
