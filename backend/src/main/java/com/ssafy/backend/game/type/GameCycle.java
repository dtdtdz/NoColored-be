package com.ssafy.backend.game.type;


public enum GameCycle {
    CREATE {
        @Override
        public GameCycle next() {
            return READY;
        }
    },
    READY {
        @Override
        public GameCycle next() {
            return PLAY;
        }
    },
    PLAY {
        @Override
        public GameCycle next() {
            return CLOSE;
        }
    },
    CLOSE {
        @Override
        public GameCycle next() {
            return CLOSE;
        }
    };

    // 모든 열거형 상수가 구현해야 하는 추상 메서드
    public abstract GameCycle next();
}
